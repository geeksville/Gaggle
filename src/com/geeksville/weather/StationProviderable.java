package com.geeksville.weather;

import java.util.List;

import org.osmdroid.util.GeoPoint;

import com.geeksville.weather.overlay.WeatherStationsOverlay;

public interface StationProviderable {
	public WeatherStationsOverlay getOverlay();
	public void setOverlay(WeatherStationsOverlay overlay);
	public List<Station> getAllStations();
	public List<Station> getStationsBBox(GeoPoint topleft, GeoPoint bottomright);
}
