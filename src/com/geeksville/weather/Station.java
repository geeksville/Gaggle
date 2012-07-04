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
package com.geeksville.weather;

import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

import com.geeksville.gaggle.R;

import android.content.Context;
import android.graphics.drawable.Drawable;

public abstract class Station extends ExtendedOverlayItem implements Stationable {

	final protected GeoPoint location;
	final protected String name;

	public Station(String aTitle, String aDescription, GeoPoint aGeoPoint,
			Context context) {
		super(aTitle, aDescription, aGeoPoint, context);
		Drawable marker = context.getResources().getDrawable(R.drawable.marker_node);
		setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
		setMarker(marker);
		location = aGeoPoint;
		name = aTitle;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getAltitude(){
		return location.getAltitude();
	}

	@Override
	public final GeoPoint getLocation() {
		return location;
	}
}
