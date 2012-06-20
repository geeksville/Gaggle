package com.geeksville.location.baro;

import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.geeksville.location.IBarometerClient;

public class DummyBarometerClient extends Observable implements IBarometerClient, Runnable {

	private float currentAlt = 500;
	private float currentVz = 0.0F;
	private float currentPs = 1024F;
	private float currentBat = 3.3F;
	private float currentBatPrct = .9F;
	private Thread thread;

	private boolean isStopped = false;

	public DummyBarometerClient(Context context){
		Log.d("DummyBaro", "Created");
	}

	@Override
	public void addObserver(Observer observer) {
		Log.d("DummyBaro", "new obs");
		if (this.countObservers() == 0){
			Log.d("DummyBaro", "new worker thread");
			isStopped = false;
		    thread = new Thread(this, "BluetoothBaro");
		    thread.setDaemon(true);
		    thread.start();
		}
		super.addObserver(observer);
	}

	@Override
	public void deleteObserver(Observer observer) {
		Log.d("DummyBaro", "delete obs");
		super.deleteObserver(observer);

		if (this.countObservers() == 0){
			Log.d("DummyBaro", "stop worker thread");
			isStopped = true;
		}
	}

	@Override
	public void setAltitude(float meters) {
	}

	@Override
	public float getAltitude() {
		currentAlt += (0.5-Math.random())*10;
		return currentAlt;
	}

	@Override
	public float getVerticalSpeed() {
		currentVz += (0.5-Math.random()) * 0.1;
		return currentVz;
	}

	@Override
	public void improveLocation(Location l) {
	}

	@Override
	public float getPressure() {
		return currentPs;
	}

	@Override
	public float getBattery() {
		return currentBat;
	}

	@Override
	public float getBatteryPercent() {
		return currentBatPrct;
	}

	@Override
	public void run() {
		while (! isStopped) {
			try {
				Thread.sleep(500);
				setChanged();
				notifyObservers(getPressure());
			} catch (InterruptedException e) {
				// loop back... :)
			}
		}
	}
}
