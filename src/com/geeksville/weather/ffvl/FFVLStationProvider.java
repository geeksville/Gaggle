package com.geeksville.weather.ffvl;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.util.GeoPoint;

import com.geeksville.weather.Station;
import com.geeksville.weather.StationProviderable;

public class FFVLStationProvider implements StationProviderable {

	private ArrayList<Station> stations = new ArrayList<Station>();
	
	@Override
	public List<Station> getAllStations() {
		return stations;
	}

	@Override
	public List<Station> getStationsBBox(GeoPoint topleft, GeoPoint bottomright) {
		return stations;
	}

}
