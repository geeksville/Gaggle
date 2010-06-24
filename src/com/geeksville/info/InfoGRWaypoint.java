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

import com.geeksville.gaggle.GaggleApplication;
import com.geeksville.location.ExtendedWaypoint;
import com.geeksville.location.Waypoint;

/**
 * Information glide needed to reach a waypoint
 * 
 * @author kevinh
 * 
 *         Subclasses should set dest, otherwise this class is not useful.
 */
public class InfoGRWaypoint extends InfoField implements Observer {

	// until we get inited

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.geeksville.info.GPSField#onCreate(android.app.Activity)
	 */
	@Override
	public void onCreate(Activity context) {
		// TODO Auto-generated method stub
		super.onCreate(context);
	}

	/**
	 * Stop listening to the GPS
	 */
	@Override void onHidden() {
		GaggleApplication app = (GaggleApplication) context.getApplication();
		app.getWaypoints().deleteObserver(this);

		super.onHidden();
	}

	@Override void onShown() {
		super.onShown();

		// We now care about waypoints moving around
		GaggleApplication app = (GaggleApplication) context.getApplication();
		app.getWaypoints().addObserver(this);
	}

	@Override
	public String getLabel() {
		return "GR needed for Wpt";
	}

	/**
	 * 
	 * @see com.geeksville.info.InfoField#getLabel()
	 */
	@Override
	public String getShortLabel() {
		return "GR req Wpt";
	}

	/**
	 * 
	 * @see com.geeksville.info.InfoField#getUnits()
	 */
	@Override
	public String getUnits() {
		// tricky way to show as a ratio
		return ":1";
	}

	/**
	 * default to showing the current waypoint
	 * 
	 * @return
	 */
	protected ExtendedWaypoint getWaypoint() {
		if (context != null) {
			GaggleApplication app = (GaggleApplication) context.getApplication();
			return app.currentDestination;
		} else
			return null;
	}

	/**
	 * 
	 * @see com.geeksville.info.InfoField#getText()
	 */
	@Override
	public String getText() {

		ExtendedWaypoint w = getWaypoint();
		if (w == null)
			return "---"; // No waypoint set

		// FIXME - draw in yellow for warning or red for below glideslope
		return w.glideRatioString();
	}

	@Override
	public int getTextColor() {
		ExtendedWaypoint w = getWaypoint();
		if (w == null)
			return super.getTextColor();

		return w.getColorRGBA();
	}

	@Override
	public void update(Observable observable, Object data) {

		Waypoint w = getWaypoint();
		if (w == null)
			return; // No point in publishing updates without a waypoint

		onChanged();
	}
}
