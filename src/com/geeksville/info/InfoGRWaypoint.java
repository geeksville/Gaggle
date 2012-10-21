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
package com.geeksville.info;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;

import com.geeksville.gaggle.GaggleApplication;
import com.geeksville.gaggle.R;
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
	@Override
	void onHidden() {
		GaggleApplication app = (GaggleApplication) context.getApplication();
		app.getWaypoints().deleteObserver(this);

		super.onHidden();
	}

	@Override
	void onShown() {
		super.onShown();

		// We now care about waypoints moving around
		GaggleApplication app = (GaggleApplication) context.getApplication();
		app.getWaypoints().addObserver(this);
	}

	@Override
	public String getLabel() {
		return context.getString(R.string.gr_needed_for_wpt);
	}

	/**
	 * 
	 * @see com.geeksville.info.InfoField#getLabel()
	 */
	@Override
	public String getShortLabel() {
		return context.getString(R.string.gr_needed_for_wpt_short);
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

		return w.glideRatioString();
	}

	@Override
	public int getTextColor() {
		ExtendedWaypoint w = getWaypoint();
		if (w == null)
			return super.getTextColor();

		int color = w.getColorRGBA();

		// Regardless of what the color specified, for this application we don't
		// want any alpha blending on our black background
		color |= 0xff000000;

		return color;

	}

	@Override
	public void update(Observable observable, Object data) {

		Waypoint w = getWaypoint();
		if (w == null)
			return; // No point in publishing updates without a waypoint

		onChanged();
	}
}
