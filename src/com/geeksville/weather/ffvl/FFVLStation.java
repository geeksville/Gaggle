package com.geeksville.weather.ffvl;

import java.util.Map;

import org.osmdroid.util.GeoPoint;
import android.content.Context;

import com.geeksville.weather.Station;

public class FFVLStation extends Station {
	private final Map<String,String> extra;
	private final boolean enabled;
	private FFVLMeasure mMeasure;
	public final int id;

	public FFVLStation(int id, String name, GeoPoint location, Map<String,String> extra, boolean enabled, Context context) {
		super(name, name, location, context);
		this.id = id;
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
		return mMeasure;
	}

	public void setFFVLMeasure(final FFVLMeasure measure){
		this.mMeasure = measure;
		this.setSubDescription("Max:" + mMeasure.getWindSpeedMax() + "\n" +
				"Avg:" + mMeasure.getWindSpeedAvg());
	}
}
