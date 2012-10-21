/****************************************************************************************
 * Gaggle is Copyright 2010, 2011, and 2012 by Kevin Hester of Geeksville Industries LLC,
 * a California limited liability corporation. 
 * 
 * Gaggle is free software: you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation, either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * Gaggle is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE.  See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with Gaggle 
 * included in this distribution in the manual (assets/manual/gpl-v3.txt). If not, see  
 * <http://www.gnu.org/licenses/> or at <http://gplv3.fsf.org>.
 ****************************************************************************************/
package com.geeksville.location;

import org.andnav.osm.util.GeoPoint;

import android.database.Cursor;
import android.graphics.drawable.Drawable;

public class ExtendedWaypoint extends Waypoint implements Comparable<ExtendedWaypoint> {

	/**
	 * When used in a competition route, this number may be non zero (in meters)
	 */
	public int diameter = 0;

	/**
	 * The vertical distance from the pilot (if pilot is higher this # will be
	 * negative)
	 */
	public int distanceFromPilotY;

	/**
	 * The horizontal distance in meters from the pilot (or -1 for unknown)
	 */
	public int distanceFromPilotX = -1;

	public GeoPoint geoPoint;

	private WaypointDB db;

	public enum Color {
		SAFE, WARNING, DANGER
	};

	private Color color = Color.WARNING;

	public ExtendedWaypoint(Cursor pts) {
		super(pts);

		init();
	}

	public ExtendedWaypoint(String name, double latitude, double longitude, int altitude, int type) {
		this(name, latitude, longitude, altitude, 0, type);
	}

	public ExtendedWaypoint(String name, double latitude, double longitude, int altitude,
			int diameter, int type) {
		super(name, latitude, longitude, altitude, type);

		this.diameter = diameter;

		init();
	}

	void setOwner(WaypointDB db) {
		this.db = db;

		// Calc extended fields
		recalculate();
	}

	private void init() {
		geoPoint = new GeoPoint((int) (latitude * 1e6), (int) (longitude * 1e6));
	}

	/**
	 * Generate occasionally updated properties
	 * 
	 * Used only by WaypointDB
	 */
	void recalculate() {

		if (db.pilotLoc == null) {
			distanceFromPilotX = -1;
			distanceFromPilotY = -1;
			color = Color.WARNING;
		} else {
			// FIXME - someday do all this math in udegrees
			float mylat = (float) (db.pilotLoc.getLatitudeE6() / 1e6);
			float mylong = (float) (db.pilotLoc.getLongitudeE6() / 1e6);

			float destlat = (float) latitude;
			float destlong = (float) longitude;

			distanceFromPilotX = (int) LocationUtils.LatLongToMeter(mylat, mylong, destlat,
					destlong);
			distanceFromPilotY = altitude - db.pilotAlt;

			color = calcColor();
		}
	}

	/**
	 * We want points closest to the pilot to be sorted to the beginning of the
	 * collection (If we are missing distance info, compare by name)
	 */
	@Override
	public int compareTo(ExtendedWaypoint another) {

		if ((another.distanceFromPilotX == -1 || distanceFromPilotX == -1) && name != null
				&& another.name != null)
			return name.compareTo(another.name);

		return distanceFromPilotX - another.distanceFromPilotX;
	}

	/**
	 * This waypoint has been changed and should be written back to the DB
	 */
	public void commit() {
		db.updateWaypoint(this);
	}

	private float glideRatioFromPilot() {
		// If we are below the point, we can't possibly glide to it
		// If we don't know where the pilot is claim we can't glide to it
		if (distanceFromPilotY >= 0)
			return Float.POSITIVE_INFINITY;

		return distanceFromPilotX / -distanceFromPilotY;
	}

	public String glideRatioString() {
		if (distanceFromPilotX == -1)
			return "---"; // We don't know where the pilot is

		float ratio = glideRatioFromPilot();

		// If we are below the point, we can't possibly glide to it
		if (Float.isInfinite(ratio))
			return "\u221E"; // infinity symbol

		// FIXME - draw in yellow for warning or red for below glideslope
		return String.format("%.1f", ratio);
	}

	/**
	 * How should we show this waypoint (SAFE, WARNING etc...)
	 * 
	 * @return
	 */
	private Color calcColor() {
		Color color;

		// If we don't yet know the pilot location, assume warn
		if (distanceFromPilotX == -1)
			return Color.WARNING;

		// If we are below the point, we can't possibly glide to it
		if (distanceFromPilotY >= 0)
			color = Color.DANGER;
		else {
			int distanceBelowMinAlt = (int) (distanceFromPilotY + db.minAltMargin);
			int distanceBelowSafeAlt = (int) (distanceFromPilotY + db.minAltMargin + db.extraAltMargin);

			float minGlideRatio = (distanceBelowMinAlt >= 0) ? Float.POSITIVE_INFINITY
					: distanceFromPilotX
							/ -distanceBelowMinAlt;
			float safeGlideRatio = (distanceBelowSafeAlt >= 0) ? Float.POSITIVE_INFINITY
					: distanceFromPilotX
							/ -distanceBelowSafeAlt;

			if (db.typicalGlideRatio >= safeGlideRatio)
				color = Color.SAFE;
			else if (db.typicalGlideRatio >= minGlideRatio)
				color = Color.WARNING;
			else
				color = Color.DANGER;
		}

		return color;
	}

	/**
	 * How should we show this waypoint (SAFE, WARNING etc...)
	 * 
	 * @return
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Return an android color id for this waypoint
	 * 
	 * @return
	 */
	public int getColorRGBA() {
		return db.markerColors[getColor().ordinal()];
	}

	/**
	 * Get an icon that represents this waypoint
	 * 
	 * @return
	 * 
	 *         We adjust color to show WARN or DANGER if the dest point is too
	 *         high
	 */
	public Drawable getIcon() {
		return getIcon(getColor());
	}

	/**
	 * Get an icon that represents this waypoint
	 * 
	 * @return
	 */
	private Drawable getIcon(Color color) {
		Drawable marker = db.markers[type.ordinal()][color.ordinal()];

		// FIXME - return a drawable that has been modified to show
		// distance/glide ratio
		return marker;
	}

}
