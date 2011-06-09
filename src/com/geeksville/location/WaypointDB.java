/*******************************************************************************
 * Gaggle is Copyright 2010 by Geeksville Industries LLC, a California limited liability corporation. 
 * 
 * Gaggle is distributed under a dual license.  We've chosen this approach because within Gaggle we've used a number
 * of components that Geeksville Industries LLC might reuse for commercial products.  Gaggle can be distributed under
 * either of the two licenses listed below.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details. 
 * 
 * Commercial Distribution License
 * If you would like to distribute Gaggle (or portions thereof) under a license other than 
 * the "GNU General Public License, version 2", contact Geeksville Industries.  Geeksville Industries reserves
 * the right to release Gaggle source code under a commercial license of its choice.
 * 
 * GNU Public License, version 2
 * All other distribution of Gaggle must conform to the terms of the GNU Public License, version 2.  The full
 * text of this license is included in the Gaggle source, see assets/manual/gpl-2.0.txt.
 ******************************************************************************/
package com.geeksville.location;

import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedMap;
import java.util.TreeMap;

import org.andnav.osm.util.GeoPoint;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import com.geeksville.android.PreferenceUtil;
import com.geeksville.gaggle.GagglePrefs;
import com.geeksville.gaggle.R;

/**
 * Provide high level API to find nearby waypoints
 * 
 * @author kevinh
 * 
 *         This class keeps a cache in memory for high speed queries about
 *         waypoints
 * 
 *         We will notify observers whenever the database has been updated and
 *         waypoint relative distances may have changed
 */
public class WaypointDB extends Observable implements LocationListener, ServiceConnection {
	private LocationLogDbAdapter db;

	private HashMap<Long, ExtendedWaypoint> wptsById = new HashMap<Long, ExtendedWaypoint>();
	private SortedMap<String, ExtendedWaypoint> wptsByName;

	private Context context;
	private IGPSClient gps;

	/**
	 * One row for each Waypoint type, one col for each WaypointColor
	 */
	Drawable[][] markers;

	/**
	 * One row for each Waypoint type, one col for each WaypointColor
	 */
	private static final int[][] markerIds = {
			{ R.drawable.blue, R.drawable.yellow, R.drawable.red },
			{ R.drawable.lz_blue, R.drawable.lz_yellow, R.drawable.lz_red },
			{ R.drawable.plane_blue, R.drawable.plane_yellow, R.drawable.plane_red },
			{ R.drawable.flag, R.drawable.flag, R.drawable.flag } };

	/**
	 * The ids for colors used when viewing text waypoint names
	 */
	private static final int[] markerColorIds = { R.color.glide_safe, R.color.glide_warn,
			R.color.glide_danger };

	int[] markerColors = new int[markerColorIds.length];

	int pilotAlt;

	/**
	 * Or null for unknown
	 */
	public GeoPoint pilotLoc;

	/**
	 * Read from glideratio_pref but we reread each pilot update to get the
	 * latest value
	 * 
	 */
	public float typicalGlideRatio, minAltMargin, extraAltMargin;

	private long minTimeMs = 30 * 1000;
	private float minDistMeters = 100.0f;

	public WaypointDB(Context context, LocationLogDbAdapter db) {
		this.db = db;
		this.context = context;
		
		GagglePrefs prefs = new GagglePrefs(context);
		minTimeMs = prefs.getLogTimeInterval();
		minDistMeters = prefs.getLogDistanceInterval();
		
		
		// FIXME - reread when prefs change
		typicalGlideRatio = PreferenceUtil.getFloat(context, "glideratio_pref", 7.0f);
		minAltMargin = PreferenceUtil.getFloat(context, "altmargin_min_pref", 0.0f);
		extraAltMargin = PreferenceUtil.getFloat(context, "altmargin_extra_pref", 100f);
		
		markers = new Drawable[Waypoint.Type.values().length][ExtendedWaypoint.Color.values().length];
		for (int wpType = 0; wpType < Waypoint.Type.values().length; wpType++)
			for (int wpColor = 0; wpColor < ExtendedWaypoint.Color.values().length; wpColor++)
				markers[wpType][wpColor] = context.getResources().getDrawable(
						markerIds[wpType][wpColor]);

		for (int wpColor = 0; wpColor < ExtendedWaypoint.Color.values().length; wpColor++)
			markerColors[wpColor] = context.getResources().getColor(markerColorIds[wpColor]);

		// FIXME - use a better data structure
		readDB(db);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observable#addObserver(java.util.Observer)
	 */
	@Override
	public synchronized void addObserver(Observer observer) {
		super.addObserver(observer);

		if (countObservers() == 1) {
			GPSClient.bindTo(context, this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observable#deleteObserver(java.util.Observer)
	 */
	@Override
	public synchronized void deleteObserver(Observer observer) {
		// TODO Auto-generated method stub
		super.deleteObserver(observer);

		if (countObservers() == 0) {
			if (gps != null)
				gps.removeLocationListener(this);

			GPSClient.unbindFrom(context, this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.content.ServiceConnection#onServiceConnected(android.content.
	 * ComponentName, android.os.IBinder)
	 */
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		gps = (IGPSClient) service;
		gps.addLocationListener(minTimeMs, minDistMeters, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.content.ServiceConnection#onServiceDisconnected(android.content
	 * .ComponentName)
	 */
	@Override
	public void onServiceDisconnected(ComponentName name) {
		gps = null;
	}

	/**
	 * Update the pilots location - used to calculate distances etc...
	 * 
	 * @param l
	 *            FIXME - call this function only rarely
	 */
	private void setPilotLocation(Location l) {
		pilotAlt = (int) l.getAltitude();
		pilotLoc = new GeoPoint((int) (l.getLatitude() * 1e6), (int) (l.getLongitude() * 1e6));

		synchronized (wptsById) {
			for (ExtendedWaypoint w : wptsById.values()) {
				w.recalculate();
			}
		}

		// Waypoint distances might have changed (FIXME, only publish if there
		// has been an appreciable change in
		// position)
		setChanged();
		notifyObservers();
	}

	public ExtendedWaypoint getNearestLZ() {
		// FIXME - use some sort of tree or heap that we can update
		// inexpensively. - also keep lzs only in that heap
		int dist = Integer.MAX_VALUE;
		ExtendedWaypoint closest = null;

		synchronized (wptsById) {
			for (ExtendedWaypoint w : wptsById.values()) {
				if (w.type == Waypoint.Type.Landing && w.distanceFromPilotX < dist) {
					dist = w.distanceFromPilotX;
					closest = w;
				}
			}
		}

		return closest;
	}

	/**
	 * Add a new waypoint to the DB & cache
	 * 
	 * @param w
	 */
	public long add(ExtendedWaypoint w) {

		w.id = db.addWaypoint(w.name, w.description, w.latitude, w.longitude, w.altitude, w.type
				.ordinal());
		addToCache(w);

		return w.id;
	}

	/**
	 * Return our name cache (generating if necessary)
	 * 
	 * @return
	 */
	public SortedMap<String, ExtendedWaypoint> getNameCache() {
		SortedMap<String, ExtendedWaypoint> map = wptsByName;

		if (map == null) {
			wptsByName = map = new TreeMap<String, ExtendedWaypoint>();

			synchronized (wptsById) {
				for (ExtendedWaypoint w : wptsById.values())
					map.put(w.name, w);
			}
		}
		return map;
	}

	public ExtendedWaypoint findByName(String name) {

		return getNameCache().get(name);
	}

	/**
	 * Update our various flavors of cache
	 * 
	 * @param w
	 */
	private void addToCache(ExtendedWaypoint w) {
		w.setOwner(this);

		synchronized (wptsById) {
			wptsById.put(w.id, w);
		}

		wptsByName = null; // flush our by name cache
	}

	/**
	 * Read all the waypoints from the db (possibly move elsewhere and/or
	 * restrict the set of read waypoints)
	 * 
	 * @param db
	 * @return
	 */
	private void readDB(LocationLogDbAdapter db) {
		// First get all the flight info
		Cursor pts = db.fetchWaypoints();

		int numPts = pts.getCount();

		for (int i = 0; i < numPts; i++) {

			ExtendedWaypoint w = new ExtendedWaypoint(pts);
			addToCache(w);
			pts.moveToNext();
		}

		pts.close();
	}

	/**
	 * Get all the waypoints but sorted by distance
	 * 
	 * @return
	 */
	public WaypointCursor fetchWaypointsByDistance() {
		return new WaypointCursor(wptsById.values());
	}

	@Override
	public void onLocationChanged(Location location) {
		// We can only use this report if it has altitude
		if (location.hasAltitude())
			setPilotLocation(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	public void deleteAllWaypoints() {
		db.deleteAllWaypoints();

		// FIXME - use proper thread safety prims for these containers
		wptsById.clear();
		wptsByName = null;
	}

	public void deleteWaypoint(long id) {
		// TODO Auto-generated method stub
		db.deleteWaypoint(id);

		wptsById.remove(id);
		wptsByName = null;
	}

	/**
	 * Callback from waypoint
	 * 
	 * @param w
	 */
	void updateWaypoint(ExtendedWaypoint w) {
		db.updateWaypoint(w.id, w.name, w.description, w.latitude, w.longitude, w.altitude, w.type.ordinal());

		wptsByName = null;
	}

}
