//Copyright (C) 2012  Marc Poulhiès
//
//This program is free software; you can redistribute it and/or
//modify it under the terms of the GNU General Public License
//as published by the Free Software Foundation; either version 2
//of the License, or (at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
