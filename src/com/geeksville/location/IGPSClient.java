/****************************************************************************************
 * Gaggle is Copyright 2010, 2011, and 2012 by Kevin Hester of Geeksville Industries LLC 
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
