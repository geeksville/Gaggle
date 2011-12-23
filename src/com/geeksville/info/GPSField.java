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

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.geeksville.gaggle.GagglePrefs;
import com.geeksville.location.GPSClient;
import com.geeksville.location.IGPSClient;

/**
 * An info field that subscribes for GPS updates
 * 
 * @author kevinh
 * 
 *         FIXME - return a no sat drawable when the GPS is disabled FIXME -
 *         when not valid, grey out text?
 */
public abstract class GPSField extends InfoField implements LocationListener, ServiceConnection {

	protected long minTimeMs = 5 * 1000;
	protected float minDistMeters = 5.0f;

	private IGPSClient gps;

	boolean isShown = false;

	/**
	 * Stop listening to the GPS
	 */
	@Override
	void onHidden() {
		super.onHidden();

		if (isShown) {
			if (gps != null)
				gps.removeLocationListener(this);

			Log.d("GPSField", "Hide " + getClass().toString());

			isShown = false;
			if (context instanceof Activity) // To work in eclipse
				GPSClient.unbindFrom(context, this);
		}
	}

	@Override
	void onShown() {
		super.onShown();

		if (!isShown) {
			Log.d("GPSField", "Show " + getClass().toString());

			if (context instanceof Activity) // To work in eclipse
				GPSClient.bindTo(context, this);

			isShown = true;
		}
	}

	/**
	 * Called to tell subclass about new location data
	 * 
	 * @param location
	 */
	public abstract void onLocationChanged(Location location);

	@Override
	public void onProviderDisabled(String provider) {
		// Do nothing
	}

	@Override
	public void onProviderEnabled(String provider) {
		// Do nothing
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// Do nothing
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

		GagglePrefs prefs = new GagglePrefs(context);
		
		minTimeMs = prefs.getScreenUpdateFreq();
		minDistMeters = prefs.getScreenUpdateDist();
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
}
