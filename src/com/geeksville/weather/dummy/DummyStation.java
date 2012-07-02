package com.geeksville.weather.dummy;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import org.osmdroid.util.GeoPoint;

import android.content.Context;

import com.geeksville.weather.Station;

public class DummyStation extends Station {

	public DummyStation(String aTitle, String aDescription, GeoPoint aGeoPoint,
			Context context) {
		super(aTitle, aDescription, aGeoPoint, context);
	}

	private final static GeoPoint location = new GeoPoint(45.194277, 5.731634, 1000);
	private final static HashMap<String,String> extra = new HashMap<String,String>();

	@Override
	public final Map<String, String> getExtraInfo() {
		return extra;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public final DummyMeasure getMeasure() {
		return new DummyMeasure(Date.valueOf("2012-06-29"), 25, 35, 20, 245, 240, 35, 95, 980, 65);
	}
}
