/****************************************************************************************
 * Gaggle is Copyright 2010, 2011, and 2012 by Kevin Hester of Geeksville Industries LLC 
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
import com.geeksville.location.LocationUtils;

public class InfoGlideRatio extends GPSField {

	@Override
	public String getLabel() {
		return context.getString(R.string.glide_ratio);
	}

	/**
	 * 
	 * @see com.geeksville.info.InfoField#getLabel()
	 */
	@Override
	public String getShortLabel() {
		return context.getString(R.string.glide_ratio_short);
	}

	/**
	 * We claim our units are :1 because it will look real slick in the GUI
	 * 
	 * @see com.geeksville.info.InfoField#getUnits()
	 */
	@Override
	public String getUnits() {
		return ":1";
	}

	/**
	 * If our glide is higher than this we presume we are not really decending
	 */
	static final float maxGlide = 100.0f;

	/**
	 * 
	 * @see com.geeksville.info.InfoField#getText()
	 */
	@Override
	public String getText() {
		float dz = distz, dxy = distxy; // cache values for thread safety

		if (Float.isNaN(dz))
			return "---"; // If we don't know our altitude, can't tell GR

		if (dz >= 0) // We are going up, so the glide is currently infinite
			return "\u221E"; // infinity symbol

		// we use a negative z to mean going down, but we want our ratio to be
		// positive
		float ratio = dxy / -dz;

		// If glide ratio is higher than any real vehicle, max out at that value
		ratio = Math.min(ratio, maxGlide);

		return String.format("%.1f", ratio);
	}

	float oldLat, oldLong;
	float newLat, newLong;
	double oldAlt, newAlt;
	float distxy, distz;

	@Override
	public void onLocationChanged(Location location) {
		// FIXME - do some averaging of points, this will look like crap
		oldLat = newLat;
		oldLong = newLong;
		oldAlt = newAlt;

		newLat = (float) location.getLatitude();
		newLong = (float) location.getLongitude();
		newAlt = location.getAltitude();

		distz = location.hasAltitude() ? (float) (newAlt - oldAlt) : Float.NaN;
		distxy = (float) LocationUtils.LatLongToMeter(oldLat, oldLong, newLat, newLong);

		onChanged();
	}
}
