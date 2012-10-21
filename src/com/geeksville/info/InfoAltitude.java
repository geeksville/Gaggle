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

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.location.Location;

import com.geeksville.gaggle.R;
import com.geeksville.location.BarometerClient;
import com.geeksville.location.IBarometerClient;

/// FIXME - show either baro or GPS based altitude?
public class InfoAltitude extends GPSField implements Observer {

	private IBarometerClient baro;

	public InfoAltitude() {
		// minDistMeters = 0; // We want updates even if horizontal pos hasn't
		// changed
	}

	/**
	 * 
	 * @see com.geeksville.info.InfoField#getLabel()
	 */
	@Override
	public String getLabel() {
		return context.getString(R.string.altitude);
	}

	/**
	 * 
	 * @see com.geeksville.info.InfoField#getLabel()
	 */
	@Override
	public String getShortLabel() {
		return context.getString(R.string.altitude_short);
	}

	/**
	 * 
	 * @see com.geeksville.info.InfoField#getUnits()
	 */
	@Override
	public String getUnits() {
		// TODO Auto-generated method stub
		return Units.instance.getAltitudeUnits();
	}

	double altMeters = Double.NaN;

	/**
	 * 
	 * @see com.geeksville.info.InfoField#getText()
	 */
	@Override
	public String getText() {
		if (Double.isNaN(altMeters))
			return "---";

		return Units.instance.metersToAltitude(altMeters);
	}

	/**
	 * @see com.geeksville.info.InfoField#onCreate(android.app.Activity)
	 */
	@Override
	public void onCreate(Activity context) {
		super.onCreate(context);

		if (context != null) {
			// FIXME - we should share one compass client object
			baro = BarometerClient.create(context);
		}
	}

	/**
	 * @see com.geeksville.info.InfoField#onHidden()
	 */
	@Override
	void onHidden() {
		super.onHidden();

		if (baro != null)
			baro.deleteObserver(this);
	}

	/**
	 * @see com.geeksville.info.InfoField#onShown()
	 */
	@Override
	void onShown() {
		super.onShown();

		if (baro != null)
			baro.addObserver(this);
	}

	// / Handle updates from GPS
	@Override
	public void onLocationChanged(Location location) {
		double naltMeters = location.hasAltitude() ? location.getAltitude() : Double.NaN;

		if (naltMeters != altMeters) {
			altMeters = naltMeters;

			onChanged();
		}
	}

	// / Handle updates from barometer
	@Override
	public void update(Observable observable, Object data) {

		float nalt = baro.getAltitude();

		if (nalt != altMeters) {
			altMeters = nalt;

			onChanged();
		}
	}
}
