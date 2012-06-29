package com.geeksville.weather;

import java.util.Map;

import org.osmdroid.util.GeoPoint;

public interface Stationable {
	public String getName();
	public int getAltitude();
	public Measure getMeasure();
	public GeoPoint getLocation();
	public Map<String, String> getExtraInfo();
	public boolean isEnabled();
}
