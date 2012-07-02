package com.geeksville.weather.overlay;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.views.MapView;

import com.geeksville.gaggle.R;
import com.geeksville.weather.Station;
import com.geeksville.weather.StationProviderable;

import android.content.Context;

public class WeatherStationsOverlay extends ItemizedOverlayWithBubble<Station> {
	private final MapView mapView;
	
	public WeatherStationsOverlay(
			Context pContext, MapView mapView,
			StationProviderable provider) {
		super(pContext, new ArrayList<Station>(), mapView, R.layout.bonuspack_bubble);
		provider.setOverlay(this);
		this.mapView = mapView;
	}

	@Override
	public boolean addItems(List<Station> items){
		final boolean r = super.addItems(items);
		// FIXME this should not be needed, as populate() called
		// in and by ItemizedIconOverlay should do the job.
		this.mapView.invalidate();
		return r;
	}
}
