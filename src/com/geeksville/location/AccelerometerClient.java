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
import android.hardware.*;

public class AccelerometerClient extends SensorClient {

	public AccelerometerClient(Context context) {

		super(context, Sensor.TYPE_ACCELEROMETER);
	}
	float[] gravity = {0,0,0};
	private boolean firstpass = true;
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
			if(firstpass)
				{
				force = 0;
				firstpass = false;
				}
			else
				force = Math.abs(xAccel) + Math.abs(yAccel) + Math.abs(zAccel);
			setChanged();
			notifyObservers(force);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		//  Auto-generated method stub
		
	}

}
