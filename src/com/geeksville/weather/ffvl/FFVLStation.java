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
package com.geeksville.weather.ffvl;

import java.util.Map;

import org.osmdroid.util.GeoPoint;
import android.content.Context;

import com.geeksville.weather.Station;

public class FFVLStation extends Station {
	private final Map<String,String> extra;
	private final boolean enabled;
	private FFVLMeasure mMeasure;
	public final int id;

	public FFVLStation(int id, String name, GeoPoint location, Map<String,String> extra, boolean enabled, Context context) {
		super(name, name, location, context);
		this.id = id;
		this.extra = extra;
		this.enabled = enabled;
	}

	@Override
	public final Map<String, String> getExtraInfo() {
		return extra;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public final FFVLMeasure getMeasure() {
		return mMeasure;
	}

	public void setFFVLMeasure(final FFVLMeasure measure){
		this.mMeasure = measure;
		this.setSubDescription("Max:" + mMeasure.getWindSpeedMax() + "\n" +
				"Avg:" + mMeasure.getWindSpeedAvg());
	}
}
