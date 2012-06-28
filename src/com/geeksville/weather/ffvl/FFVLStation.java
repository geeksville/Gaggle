package com.geeksville.weather.ffvl;

import java.util.Map;

import org.osmdroid.util.GeoPoint;

import com.geeksville.weather.Station;

public class FFVLStation extends Station {

	public FFVLStation(String aTitle, String aDescription, GeoPoint aGeoPoint) {
		super(aTitle, aDescription, aGeoPoint);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getAltitude() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public final GeoPoint getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public final Map<String, String> getExtraInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public final FFVLMeasure getMeasure() {
		// TODO Auto-generated method stub
		return null;
	}
}
