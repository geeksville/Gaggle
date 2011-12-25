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

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.location.Location;

import com.geeksville.gaggle.R;
import com.geeksville.location.BarometerClient;

/// FIXME - show either baro or GPS based altitude?
public class InfoAltitude extends GPSField implements Observer {

	private BarometerClient baro;

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
