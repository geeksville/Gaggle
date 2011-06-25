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
import android.hardware.Sensor;
import android.hardware.SensorManager;

/// FIXME - add a basic vario http://www.paraglidingforum.com/viewtopic.php?p=48465
public class BarometerClient extends SensorClient {

	/**
	 * Current compass reading
	 */
	public float pressure;

	// / Defaults to 1013.25 hPa
	public float reference = SensorManager.PRESSURE_STANDARD_ATMOSPHERE;

	public BarometerClient(Context context) {
		super(context, Sensor.TYPE_PRESSURE);
	}

	// / Given a GPS based altitude, reverse engineer what the correct reference
	// pressure is
	public void setAltitude(float meters) {
		throw new RuntimeException("setAltitude");
	}

	// / Return altitude in meters
	public float getAltitude() {
		return SensorManager.getAltitude(reference, pressure);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Auto-generated method stub

	}

	@Override
	public void onThrottledSensorChanged(float[] values) {
		pressure = values[0];

		setChanged();
		notifyObservers(pressure);
	}

}
