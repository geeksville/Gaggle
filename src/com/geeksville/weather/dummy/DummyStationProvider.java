package com.geeksville.weather.dummy;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.util.GeoPoint;

import com.geeksville.weather.Station;
import com.geeksville.weather.StationProviderable;
import com.geeksville.weather.overlay.WeatherStationsOverlay;

public class DummyStationProvider implements StationProviderable {

	private ArrayList<Station> stations = new ArrayList<Station>();
	private WeatherStationsOverlay overlay;
	
	@Override
	public List<Station> getAllStations() {
		return stations;
	}

	@Override
	public List<Station> getStationsBBox(GeoPoint topleft, GeoPoint bottomright) {
		return stations;
	}

	@Override
	public WeatherStationsOverlay getOverlay() {
		return overlay;
	}

	@Override
	public void setOverlay(WeatherStationsOverlay overlay) {
		this.overlay = overlay;
	}
}
