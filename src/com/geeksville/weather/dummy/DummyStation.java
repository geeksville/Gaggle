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

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import org.osmdroid.util.GeoPoint;

import android.content.Context;

import com.geeksville.weather.Station;

public class DummyStation extends Station {

	public DummyStation(String aTitle, String aDescription, GeoPoint aGeoPoint,
			Context context) {
		super(aTitle, aDescription, aGeoPoint, context);
	}

	private final static GeoPoint location = new GeoPoint(45.194277, 5.731634, 1000);
	private final static HashMap<String,String> extra = new HashMap<String,String>();

	@Override
	public final Map<String, String> getExtraInfo() {
		return extra;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public final DummyMeasure getMeasure() {
		return new DummyMeasure(Date.valueOf("2012-06-29"), 25, 35, 20, 245, 240, 35, 95, 980, 65);
	}
}
