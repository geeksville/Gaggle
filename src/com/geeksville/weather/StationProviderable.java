package com.geeksville.weather;

import java.util.List;

import org.osmdroid.util.GeoPoint;

public interface StationProviderable {
	public List<Station> getAllStations();
	public List<Station> getStationsBBox(GeoPoint topleft, GeoPoint bottomright);
}
