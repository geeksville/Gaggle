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

import com.geeksville.gaggle.R;
import com.geeksville.location.BarometerClient;
import com.geeksville.location.IBarometerClient;

/**
 * A simple barometer display
 * 
 * @author kevinh
 * 
 */
public class InfoBarometer extends InfoField implements Observer {

	private float pressure;

	private IBarometerClient baro;

	@Override
	public String getLabel() {
		return context.getString(R.string.barometer);
	}

	/**
	 * 
	 * @see com.geeksville.info.InfoField#getText()
	 */
	@Override
	public String getText() {
		return String.format("%.2f", pressure);
	}

	/**
	 * 
	 * @see com.geeksville.info.InfoField#getUnits()
	 */
	@Override
	public String getUnits() {
		return "mb";
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

	@Override
	public void update(Observable observable, Object data) {

		float npressure = (Float) data;

		if (npressure != pressure) {
			pressure = npressure;

			onChanged();
		}
	}

}
