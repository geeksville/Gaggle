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
