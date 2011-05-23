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
import android.hardware.SensorListener;
import android.hardware.SensorManager;

abstract class SensorClient extends Observable implements SensorEventListener {

	private SensorManager sensorMan;

	private int sensorType;

	protected int timeSpanMs = 500;

	public SensorClient(Context context, int sensorType) {
		sensorMan = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

		this.sensorType = sensorType;
	}

	/**
	 * If you are just using observers, you do not need to call this method. It
	 * is here folks who want to poll instead
	 */
	public void stopListening() {
		sensorMan.unregisterListener(this);
	}

	/**
	 * If you are just using observers, you do not need to call this method. It
	 * is here folks who want to poll instead
	 */
	public void startListening() {
		List<Sensor> sensors = sensorMan.getSensorList(sensorType);
		if (sensors.size() > 0){
			Sensor sensor = sensors.get(0);
		sensorMan.registerListener(
				this,
				sensor,
				SensorManager.SENSOR_DELAY_NORMAL);
		}
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
