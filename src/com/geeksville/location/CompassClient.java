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
package com.geeksville.location;

import android.content.Context;
import android.hardware.Sensor;

public class CompassClient extends SensorClient {

	/**
	 * Current compass reading
	 */
	public int bearing;

	private static CompassClient instance = null;

	private CompassClient(Context context) {
		super(context, Sensor.TYPE_ORIENTATION);
	}

	public static CompassClient create(Context context) {
		if (instance == null)
			instance = new CompassClient(context);

		return instance;
	}

	@Override
	public void onThrottledSensorChanged(float[] values) {
		bearing = (int) values[0];

		setChanged();
		notifyObservers(bearing);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Auto-generated method stub
		
	}

}
