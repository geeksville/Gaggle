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
	private String status;
	
	@Override
  public String getText() {
	  status = (baro == null)?"(none)":baro.getStatus();
		return String.format("%s %.2f", status, pressure);
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
