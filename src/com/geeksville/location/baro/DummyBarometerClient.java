package com.geeksville.location.baro;

import java.util.Observer;

import android.content.Context;
import android.location.Location;

import com.geeksville.location.IBarometerClient;

public class DummyBarometerClient implements IBarometerClient {

	public DummyBarometerClient(Context context){
		
	}
	
	@Override
	public void addObserver(Observer observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteObserver(Observer observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAltitude(float meters) {
		// TODO Auto-generated method stub

	}

	@Override
	public float getAltitude() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getVerticalSpeed() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void improveLocation(Location l) {
		// TODO Auto-generated method stub

	}

	@Override
	public float getPressure() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getBattery() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getBatteryPercent() {
		// TODO Auto-generated method stub
		return 0;
	}

}
