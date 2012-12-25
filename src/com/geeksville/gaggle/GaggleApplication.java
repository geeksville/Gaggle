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
package com.geeksville.gaggle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.apache.commons.io.FileUtils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Environment;
import android.util.Log;
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

    private static GaggleApplication instance;

    public static Context getContext() {
        return instance;
    }

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
	    instance = this;
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
	    ACRA.init(this);
		super.onCreate();
		GagglePrefs prefs = new GagglePrefs(this);
		if (prefs.isFlurryEnabled()){
			FlurryAgent.setCaptureUncaughtExceptions(false);
			FlurryAgent.setReportLocation(true);
		}
		File file = getFileStreamPath("world.mbtiles");
		if (!file.exists()) {
			try {
				InputStream assetInputStream = getAssets()
						.open("world.mbtiles");
				FileUtils.copyInputStreamToFile(assetInputStream, file);
			} catch (IOException e) {
				Log.e("worldMap",
						"World map could not be coppied from assets to private file storage");
			}
		}

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
