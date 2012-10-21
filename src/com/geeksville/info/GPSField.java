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
