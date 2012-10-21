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
				SensorManager.SENSOR_DELAY_NORMAL);
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
