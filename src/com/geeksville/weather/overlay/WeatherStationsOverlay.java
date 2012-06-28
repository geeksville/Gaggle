package com.geeksville.weather.overlay;

import org.osmdroid.views.overlay.ItemizedIconOverlay;

import com.geeksville.weather.Station;
import com.geeksville.weather.StationProviderable;

import android.content.Context;

public class WeatherStationsOverlay extends ItemizedIconOverlay<Station> {

	public WeatherStationsOverlay(
			Context pContext,
			StationProviderable provider,
			org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener<Station> pOnItemGestureListener) {
		super(pContext, provider.getAllStations(), pOnItemGestureListener);
	}

}
