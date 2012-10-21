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
package com.geeksville.gaggle;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.geeksville.location.ExtendedWaypoint;
import com.geeksville.location.GPSToPositionWriter;
import com.geeksville.location.LocationLogDbAdapter;
import com.geeksville.location.WaypointDB;

/**
 * A container for shared app state
 * 
 * @author kevinh
 * 
 */
public class GaggleApplication extends Application {

	/**
	 * The waypoint that is our current destination (or null for no dest set)
	 */
	public ExtendedWaypoint currentDestination = null;

	/**
	 * We read this cache the first time someone asks for waypoints
	 */
	private WaypointDB waypoints = null;

	private GPSToPositionWriter gpsToPos;

	public synchronized WaypointDB getWaypoints() {
		// FIXME, close the backing DB when the waypoint cache is done with it

		if (waypoints == null) {
			LocationLogDbAdapter ldb = new LocationLogDbAdapter(this);
			waypoints = new WaypointDB(this, ldb);
		}

		return waypoints;
	}

	public GPSToPositionWriter getGpsLogger() {
		return gpsToPos;
	}

	public GaggleApplication() {
		gpsToPos = new GPSToPositionWriter(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		FlurryAgent.setCaptureUncaughtExceptions(false);
		FlurryAgent.setReportLocation(true);
	}

	/**
	 * Once our main GUI goes away, they call this, to ensure our service isn't
	 * left needlessly running
	 */
	public void stopGPSClient() {

	}

	/**
	 * Is the GPS enabled?
	 */
	private boolean isGPSEnabled() {
		LocationManager locMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		return locMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	/**
	 * Ask the user to turn on the GPS if necessary
	 */
	public boolean enableGPS(Activity context) {
		if (!isGPSEnabled()) {

			// Tell the user what is going on
			Toast t = Toast
					.makeText(
							context,
							"The GPS is currently disabled.  Please enable it in your system settings screen.",
							Toast.LENGTH_SHORT);
			t.show();

			context.startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
		}

		return isGPSEnabled();
	}
}
