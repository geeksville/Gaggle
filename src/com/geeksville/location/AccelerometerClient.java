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

public class AccelerometerClient extends SensorClient {

	private static AccelerometerClient instance = null;

	float[] gravity = { 0, 0, 0 };
	private boolean firstpass = true;

	private AccelerometerClient(Context context) {
		super(context, Sensor.TYPE_ACCELEROMETER);
	}

	public static AccelerometerClient create(Context context) {
		if (instance == null && isAvailable())
			instance = new AccelerometerClient(context);

		return instance;
	}

	private static boolean isAvailable() {
		return sensorMan.getSensorList(Sensor.TYPE_ACCELEROMETER).size() > 0;
	}

	@Override
	public void onThrottledSensorChanged(float[] values) {
		// alpha is calculated as t / (t + dT)
		// with t, the low-pass filter's time-constant
		// and dT, the event delivery rate
		final float alpha = 0.8f;
		gravity[0] = alpha * gravity[0] + (1 - alpha) * values[0];
		gravity[1] = alpha * gravity[1] + (1 - alpha) * values[1];
		gravity[2] = alpha * gravity[2] + (1 - alpha) * values[2];
		float xAccel = values[0] - gravity[0];
		float yAccel = values[1] - gravity[1];
		float zAccel = values[2] - gravity[2];
		float force;
		if (firstpass) {
			force = 0;
			firstpass = false;
		} else
			force = Math.abs(xAccel) + Math.abs(yAccel) + Math.abs(zAccel);
		setChanged();
		notifyObservers(force);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Auto-generated method stub

	}

}
