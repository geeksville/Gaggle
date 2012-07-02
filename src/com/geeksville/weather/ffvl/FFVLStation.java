package com.geeksville.weather.ffvl;

import java.util.Map;

import org.osmdroid.util.GeoPoint;
import android.content.Context;

import com.geeksville.weather.Station;

public class FFVLStation extends Station {
	private final Map<String,String> extra;
	private final boolean enabled;
	
	public FFVLStation(String id, String name, GeoPoint location, Map<String,String> extra, boolean enabled, Context context) {
		super(name, name, location, context);
		this.extra = extra;
		this.enabled = enabled;
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
