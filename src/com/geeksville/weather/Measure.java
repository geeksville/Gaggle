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

import java.util.Date;

public interface Measure {
	public Date getDate();
	public Float getWindSpeedAvg();
	public Float getWindSpeedMax();
	public Float getWindSpeedMin();
	public Integer getWindDirectionAvg();
	public Integer getWindDirectionInst();
	public Float getTemperature();
	public Float getHumidity();
	public Float getPressure();
	public Float getLuminosity();
}
