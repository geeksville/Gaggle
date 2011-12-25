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

import java.util.Observer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;

import com.geeksville.android.PreferenceUtil;
import com.geeksville.util.IIRFilter;
import com.geeksville.util.LinearRegression;

/// FIXME - add a basic vario http://www.paraglidingforum.com/viewtopic.php?p=48465
public class BarometerClient extends SensorClient {

	private static final String TAG = "BarometerClient";

	IIRFilter filter = new IIRFilter();

	LinearRegression regression = new LinearRegression();

	public float pressure, altitude;

	// / Defaults to 1013.25 hPa
	private float reference = SensorManager.PRESSURE_STANDARD_ATMOSPHERE;

	private Context context;

	private static BarometerClient instance = null;

	private BarometerClient(Context context) {
		super(context, Sensor.TYPE_PRESSURE);
		this.context = context;
	}

	/** Cheezy trick to apply preferences changes immediately on view change */
	@Override
	public synchronized void addObserver(Observer observer) {
		// TODO Auto-generated method stub
		super.addObserver(observer);

		// 0.20 is a little too noisy,
		// 0.05 is too stable
		filter.setDampingFactor(PreferenceUtil.getFloat(context,
				"averaging_percentage", 0.15f));

		regression.setXspan((long) (PreferenceUtil.getFloat(context,
				"integration_period", 1.0f) * 1000));
	}

	/**
	 * All users of barometer share the same (expensive) instance
	 * 
	 * @return null for if not available
	 */
	public static BarometerClient create(Context context) {
		initManager(context);

		if (instance == null && isAvailable())
			instance = new BarometerClient(context);

		return instance;
	}

	// / Given a GPS based altitude, reverse engineer what the correct reference
	// pressure is
	public void setAltitude(float meters) {
		// float p0 = 1013.25f; // Pressure at sea level (hPa)
		// float p = p0 * (float) Math.pow((1 - meters / 44330), 5.255);
		float p0 = pressure / (float) Math.pow((1 - meters / 44330), 5.255);

		reference = p0;

		Log.w(TAG, "Setting baro reference to " + reference + " alt=" + meters);
	}

	private static boolean isAvailable() {
		return sensorMan.getSensorList(Sensor.TYPE_PRESSURE).size() > 0;
	}

	// / Return altitude in meters
	public float getAltitude() {
		return altitude;
	}

	// / In m/s
	public float getVerticalSpeed() {
		return regression.getSlope() * 1000;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Auto-generated method stub
	}

	// / We want to sample _every_ baro reading we see
	@Override
	protected void fullRateSensorChanged(float[] values) {
		filter.addSample(values[0]);

		pressure = filter.get();
		altitude = SensorManager.getAltitude(reference, pressure);
		regression.addSample(System.currentTimeMillis(), altitude);

		super.fullRateSensorChanged(values);
	}

	@Override
	public void onThrottledSensorChanged(float[] values) {
		setChanged();
		notifyObservers(pressure);
	}

}
