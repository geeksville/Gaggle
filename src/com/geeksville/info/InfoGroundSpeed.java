/****************************************************************************************
 * Gaggle is Copyright 2010, 2011, and 2012 by Kevin Hester of Geeksville Industries LLC,
 * a California limited liability corporation. 
 * 
 * Gaggle is free software: you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation, either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * Gaggle is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE.  See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with Gaggle 
 * included in this distribution in the manual (assets/manual/gpl-v3.txt). If not, see  
 * <http://www.gnu.org/licenses/> or at <http://gplv3.fsf.org>.
 ****************************************************************************************/
package com.geeksville.info;

import android.location.Location;

import com.geeksville.gaggle.R;

public class InfoGroundSpeed extends GPSField {

	@Override
	public String getLabel() {
		return context.getString(R.string.speed);
	}

	/**
	 * 
	 * @see com.geeksville.info.InfoField#getLabel()
	 */
	@Override
	public String getShortLabel() {
		return context.getString(R.string.ground_speed_short);
	}

	/**
	 * 
	 * @see com.geeksville.info.InfoField#getUnits()
	 */
	@Override
	public String getUnits() {
		// TODO Auto-generated method stub
		return Units.instance.getSpeedUnits();
	}

	float metersPerSec = Float.NaN;

	/**
	 * 
	 * @see com.geeksville.info.InfoField#getText()
	 */
	@Override
	public String getText() {
		if (Float.isNaN(metersPerSec))
			return "---";

		return Units.instance.meterPerSecToSpeed(metersPerSec);
	}

	@Override
	public void onLocationChanged(Location location) {
		// Sometimes the G1 GPS will report bogus speeds, look for really large
		// values
		float maxSpeed = 83f; // 83 m/sec is 300 km/h - i.e. really damn fast

		float newSpeed = Float.NaN;

		if (location.hasSpeed()) {
			newSpeed = location.getSpeed();
			if (newSpeed > maxSpeed)
				newSpeed = Float.NaN;
		}

		float nmetersPerSec = newSpeed;
		if (nmetersPerSec != metersPerSec) {
			metersPerSec = nmetersPerSec;
			onChanged();
		}
	}
}
