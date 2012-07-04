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

import java.util.Date;

import com.geeksville.weather.Measure;

public class DummyMeasure implements Measure {

	private final Date date;
	private final float wavg, wmax, wmin, temp, humidity, ps, lumi;
	private final int wdiravg, wdir;
	
	public DummyMeasure(Date date, float wavg, float wmax, float wmin,
			int wdir, int wdiravg, float temp, float humidity, float ps, float lumi){
		this.date = date;
		this.wavg = wavg;
		this.wmax = wmax;
		this.wmin = wmin;
		this.wdir = wdir;
		this.wdiravg = wdiravg;
		this.temp = temp;
		this.humidity = humidity;
		this.ps = ps;
		this.lumi = lumi;
	}
	
	@Override
	public final Date getDate() {
		return date;
	}

	@Override
	public Float getWindSpeedAvg() {
		return wavg;
	}

	@Override
	public Float getWindSpeedMax() {
		return wmax;
	}

	@Override
	public Float getWindSpeedMin() {
		return wmin;
	}

	@Override
	public Integer getWindDirectionAvg() {
		return wdiravg;
	}

	@Override
	public Integer getWindDirectionInst() {
		return wdir;
	}

	@Override
	public Float getTemperature() {
		return temp;
	}

	@Override
	public Float getHumidity() {
		return humidity;
	}

	@Override
	public Float getPressure() {
		return ps;
	}

	@Override
	public Float getLuminosity() {
		return lumi;
	}
}