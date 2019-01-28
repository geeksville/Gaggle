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

import android.location.Location;
import android.location.LocationListener;

/**
 * The IBinder based interface for talking to ourGPS service
 * 
 * @author kevinh
 * 
 */
public interface IGPSClient {

	public Location getLastKnownLocation();

	/**
	 * Used to request location/Status updates (once we have someone asking for
	 * updates we will turn GPS on)
	 * 
	 * @param l
	 *            the callback - will be called from the background GPS thread
	 */
	void addLocationListener(long minTimeMs, float minDistMeters, LocationListener l);

	/**
	 * We are no longer interested in updates
	 * 
	 * @param l
	 */
	void removeLocationListener(LocationListener l);

	/**
	 * Tell the OS and user we are now doing an important background operation
	 * 
	 * @param tickerMsg
	 * @param notificationText
	 */
	public void startForeground(String tickerMsg, String notificationText);

	/**
	 * Tell user are no longer running a critical foreground service
	 */
	public void stopForeground();

	// public boolean isForeground();
}
