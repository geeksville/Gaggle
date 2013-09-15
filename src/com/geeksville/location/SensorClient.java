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

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

abstract class SensorClient extends Observable implements SensorEventListener {

	protected static SensorManager sensorMan;

	private int sensorType;

	protected int timeSpanMs = 500;

	public SensorClient(Context context, int sensorType) {
		initManager(context);

		this.sensorType = sensorType;

		if (getSensor() == null)
			throw new RuntimeException("Sensor not found");
	}

	protected static void initManager(Context context) {
		if (sensorMan == null)
			sensorMan = (SensorManager) context
					.getSystemService(Context.SENSOR_SERVICE);
	}

	/**
	 * If you are just using observers, you do not need to call this method. It
	 * is here folks who want to poll instead
	 */
	private void stopListening() {
		sensorMan.unregisterListener(this);
	}

	// / The hardware sensor we are connected to (or null for not found)
	public Sensor getSensor() {
		List<Sensor> sensors = sensorMan.getSensorList(sensorType);
		if (sensors.size() > 0)
			return sensors.get(0);
		else
			return null;
	}

	/**
	 * If you are just using observers, you do not need to call this method. It
	 * is here folks who want to poll instead
	 */
	private void startListening() {
		Sensor sensor = getSensor();
		sensorMan.registerListener(
				this,
				sensor,
				100000 /*100 ms*/);
				//SensorManager.SENSOR_DELAY_NORMAL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observable#addObserver(java.util.Observer)
	 */
	@Override
	public synchronized void addObserver(Observer observer) {
		if (countObservers() == 0)
			startListening();
		super.addObserver(observer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observable#deleteObserver(java.util.Observer)
	 */
	@Override
	public synchronized void deleteObserver(Observer observer) {
		super.deleteObserver(observer);

		if (countObservers() == 0)
			stopListening();
	}

	float[] values;

	/**
	 * 
	 * @return the last set of values we received from this sensor
	 */
	public float[] getValues() {
		return values;
	}

	/**
	 * Crufty - we will break if the clock is changed while we are running FIXME
	 */
	long lastUpdate = System.currentTimeMillis();

	@Override
	public void onSensorChanged(SensorEvent event) {
		this.values = event.values; // No need for a deep copy
		fullRateSensorChanged(values);
	}

	protected void fullRateSensorChanged(float[] values) {
		// We limit updates to a slowish rate to avoid burning cycles elsewhere
		long nowMs = System.currentTimeMillis();
		long diff = nowMs - lastUpdate;
		if (diff >= timeSpanMs) {
			onThrottledSensorChanged(values);
			lastUpdate = nowMs;
		}
	}

	protected abstract void onThrottledSensorChanged(float[] values);
}
