/*******************************************************************************
 * Gaggle is Copyright 2010 by Geeksville Industries LLC, a California limited liability corporation. 
 * 
 * Gaggle is distributed under a dual license.  We've chosen this approach because within Gaggle we've used a number
 * of components that Geeksville Industries LLC might reuse for commercial products.  Gaggle can be distributed under
 * either of the two licenses listed below.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details. 
 * 
 * Commercial Distribution License
 * If you would like to distribute Gaggle (or portions thereof) under a license other than 
 * the "GNU General Public License, version 2", contact Geeksville Industries.  Geeksville Industries reserves
 * the right to release Gaggle source code under a commercial license of its choice.
 * 
 * GNU Public License, version 2
 * All other distribution of Gaggle must conform to the terms of the GNU Public License, version 2.  The full
 * text of this license is included in the Gaggle source, see assets/manual/gpl-2.0.txt.
 ******************************************************************************/
package com.geeksville.info;

import android.location.Location;

public class InfoGroundSpeed extends GPSField {

	@Override
	public String getLabel() {
		return "Speed";
	}

	/**
	 * 
	 * @see com.geeksville.info.InfoField#getLabel()
	 */
	@Override
	public String getShortLabel() {
		return "GSpd";
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
