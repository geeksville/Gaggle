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
import android.location.LocationManager;

import com.geeksville.android.AndroidUtil;

/**
 * Try to find location with GPS, but fall back to cell data if necessary
 * 
 * @author khester
 * 
 */
public class BestEffortLocation implements LocationProvider {
	BasicLocation gps, cell;

	public BestEffortLocation(Context context, int updatePeriodMsecs) {
		gps = new BasicLocation(context, LocationManager.GPS_PROVIDER, updatePeriodMsecs);
		cell = new BasicLocation(context, LocationManager.NETWORK_PROVIDER, updatePeriodMsecs);
	}

	public void close() {
		gps.close();
		cell.close();
	}

	/**
	 * @see com.geeksville.location.LocationProvider#getLocation()
	 */
	public Location getLocation() {
		Location l = gps.getLocation();
		if (l != null)
			return l;

		l = cell.getLocation();
		if (l != null)
			return l;

		if (AndroidUtil.isEmulator()) {
			l = new Location("fake");
			l.setLatitude(39.644403); // Lat:39.644403 Long:-106.386702
			l.setLongitude(-106.386702);
			l.setAltitude(1500);
		}

		return l;
	}
}
