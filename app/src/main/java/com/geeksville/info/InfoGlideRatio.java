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
