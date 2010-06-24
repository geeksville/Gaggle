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
package com.geeksville.info;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.graphics.drawable.Drawable;

import com.geeksville.gaggle.GaggleApplication;
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
		// The following check is necessary to work in the IDE
		if (context != null) {
			GaggleApplication app = (GaggleApplication) context.getApplication();
			db = app.getWaypoints();

			// FIXME - we should share one compass client object
			compass = new CompassClient(context);
			image = new HeadingDrawable(context, compass);
		}

		// Do after we do our init, because it might call onResume
		super.onCreate(context);
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
		compass.deleteObserver(this);

		db.deleteObserver(this);

		super.onHidden();
	}

	@Override
	void onShown() {
		super.onShown();

		compass.addObserver(this); // FIXME, we are leaking observers, we
		// should remove ourselves later

		// We now care about waypoints moving around
		db.addObserver(this);
	}

	@Override
	public String getLabel() {
		return "Distance to Wpt";
	}

	/**
	 * 
	 * @see com.geeksville.info.InfoField#getLabel()
	 */
	@Override
	public String getShortLabel() {
		return "To Wpt";
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
		return (getWaypoint() != null && db.pilotLoc != null);
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
		return w.getColorRGBA();
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
