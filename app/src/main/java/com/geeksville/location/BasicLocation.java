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

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * A simple class that infrequntly reads the android GPS or cell data to find
 * our location
 * 
 * @author khester
 * 
 */
public class BasicLocation implements LocationListener, LocationProvider {

	private Location curLoc = null;
	private LocationManager manager;

	public BasicLocation(Context context, String providerName, int updatePeriodMsecs) {
		manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

		manager.requestLocationUpdates(providerName, updatePeriodMsecs, (float) 5.0, this);
	}

	public void close() {
		manager.removeUpdates(this);
	}

	/**
	 * @see com.geeksville.location.LocationProvider#getLocation()
	 */
	public Location getLocation() {
		return curLoc;
	}

	@Override
	public void onLocationChanged(Location location) {
		curLoc = location;
	}

	@Override
	public void onProviderDisabled(String provider) {
		curLoc = null;
	}

	@Override
	public void onProviderEnabled(String provider) {
		// ignore
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// ignore
	}

}
