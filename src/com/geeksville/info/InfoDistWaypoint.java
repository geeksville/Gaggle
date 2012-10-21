/****************************************************************************************
 * Gaggle is Copyright 2010, 2011, and 2012 by Kevin Hester of Geeksville Industries LLC 
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
package com.geeksville.info;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.graphics.drawable.Drawable;

import com.geeksville.gaggle.GaggleApplication;
import com.geeksville.gaggle.R;
import com.geeksville.location.CompassClient;
import com.geeksville.location.ExtendedWaypoint;
import com.geeksville.location.LocationUtils;
import com.geeksville.location.Waypoint;
import com.geeksville.location.WaypointDB;

/**
 * Information about a waypoint
 * 
 * @author kevinh
 * 
 *         Subclasses should set dest, otherwise this class is not useful.
 */
public class InfoDistWaypoint extends InfoField implements Observer {

	HeadingDrawable image;
	CompassClient compass;
	WaypointDB db;

	// until we get inited

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.geeksville.info.GPSField#onCreate(android.app.Activity)
	 */
	@Override
	public void onCreate(Activity context) {
		super.onCreate(context);

		// The following check is necessary to work in the IDE
		if (context != null) {
			GaggleApplication app = (GaggleApplication) context.getApplication();
			db = app.getWaypoints();

			// FIXME - we should share one compass client object
			compass = CompassClient.create(context);
			image = new HeadingDrawable(context, compass);
		}
	}

	/**
	 * default to showing the current waypoint
	 * 
	 * @return
	 */
	protected ExtendedWaypoint getWaypoint() {
		// IDE check
		if (context != null) {
			GaggleApplication app = (GaggleApplication) context.getApplication();
			return app.currentDestination;
		} else
			return null;
	}

	/**
	 * Stop listening to the GPS
	 */
	@Override
	void onHidden() {
		if (compass != null)
			compass.deleteObserver(this);

		db.deleteObserver(this);

		super.onHidden();
	}

	@Override
	void onShown() {
		super.onShown();

		if (compass != null)
			compass.addObserver(this); // FIXME, we are leaking observers, we
		// should remove ourselves later

		// We now care about waypoints moving around
		db.addObserver(this);
	}

	@Override
	public String getLabel() {
		return context.getString(R.string.distance_to_wpt);
	}

	/**
	 * 
	 * @see com.geeksville.info.InfoField#getLabel()
	 */
	@Override
	public String getShortLabel() {
		return context.getString(R.string.to_wpt);
	}

	/**
	 * 
	 * @see com.geeksville.info.InfoField#getUnits()
	 */
	@Override
	public String getUnits() {
		// TODO Auto-generated method stub
		return Units.instance.getDistanceUnits();
	}

	/**
	 * Our directional arrow
	 * 
	 * @see com.geeksville.info.InfoField#getImage()
	 */
	@Override
	public Drawable getImage() {
		if (!isValid()) // Can't show an image yet
			return null;

		return image;
	}

	/**
	 * True if we can show a bearing
	 * 
	 * @return
	 */
	private boolean isValid() {
		return (getWaypoint() != null && db.pilotLoc != null && compass != null);
	}

	/**
	 * 
	 * @see com.geeksville.info.InfoField#getText()
	 */
	@Override
	public String getText() {

		if (!isValid())
			return "---"; // No waypoint set or we don't know where we are

		// FIXME - draw in yellow for warning or red for below glideslope

		ExtendedWaypoint w = getWaypoint();
		return Units.instance.metersToDistance(w.distanceFromPilotX);
	}

	@Override
	public int getTextColor() {

		if (!isValid())
			return super.getTextColor();

		ExtendedWaypoint w = getWaypoint();
		int color = w.getColorRGBA();

		// Regardless of what the color specified, for this application we don't
		// want any alpha blending on our black background
		color |= 0xff000000;

		return color;
	}

	@Override
	public void update(Observable observable, Object data) {

		Waypoint w = getWaypoint();
		if (!isValid())
			return; // No point in publishing updates without a waypoint or
		// current loc

		double mylat = db.pilotLoc.getLatitudeE6() / 1e6;
		double mylong = db.pilotLoc.getLongitudeE6() / 1e6;

		double destlat = w.latitude;
		double destlong = w.longitude;

		// Convert to degrees
		int bearing = (int) LocationUtils.radToBearing(LocationUtils.bearing(mylat, mylong,
				destlat, destlong));

		image.setHeading(bearing);

		onChanged();
	}
}
