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

import java.util.Date;

import com.geeksville.weather.Measure;

public class FFVLMeasure implements Measure {
	private final Date date;
	private final Float wmin, wmax, wavg;
	private final Integer wdirinst, wdiravg;
	private final Float temp, hydro, press, lumi;

	public FFVLMeasure(Date date, Float wind_min, Float wind_max, Float wind_avg,
		Integer wind_dir, Integer wind_diravg,
		Float temp, Float hydro, Float press, Float lumi){
		this.date = date;
		this.wmin = wind_min;
		this.wmax = wind_max;
		this.wavg = wind_avg;
		this.wdirinst = wind_dir;
		this.wdiravg = wind_diravg;
		this.temp = temp;
		this.hydro = hydro;
		this.press = press;
		this.lumi = lumi;
	}

	@Override
	public Date getDate() {
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
		return wdirinst;
	}

	@Override
	public Float getTemperature() {
		return temp;
	}

	@Override
	public Float getHumidity() {
		return hydro;
	}

	@Override
	public Float getPressure() {
		return press;
	}

	@Override
	public Float getLuminosity() {
		return lumi;
	}
}
