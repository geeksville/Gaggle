package com.geeksville.weather.ffvl;

import java.util.Map;

import org.osmdroid.util.GeoPoint;

import android.util.Log;

import com.geeksville.weather.Station;

public class FFVLStation extends Station {
	private final Map<String,String> extra;
	private final boolean enabled;

	public FFVLStation(String id, String name, GeoPoint location, Map<String,String> extra, boolean enabled) {
		super(name, name, location);
		this.extra = extra;
		this.enabled = enabled;
		Log.d("FFVLStation", "Created station: \n" +
				"- " + id + "\n" +
				"- " + name + "\n" +
				"- lat: " + location.getLatitudeE6() + "\n" +
				"- lon: " + location.getLongitudeE6() + "\n");
	}


	@Override
	public final Map<String, String> getExtraInfo() {
		return extra;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public final FFVLMeasure getMeasure() {
		// TODO Auto-generated method stub
		return null;
	}
}
