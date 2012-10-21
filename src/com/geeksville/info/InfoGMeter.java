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
import com.geeksville.location.AccelerometerClient;

/**
 * Read the magnetic heading as measured by the accel
 * 
 * @author kevinh
 * 
 */
public class InfoGMeter extends InfoField implements Observer {

	private float g = 0.0f, gMax = 0.0f;

	private AccelerometerClient accel;

	@Override
	public String getLabel() {
		return context.getString(R.string.g_meter_label);
	}

	/**
	 * 
	 * @see com.geeksville.info.InfoField#getText()
	 */
	@Override
	public String getText() {
		return String.format("%.1f(%.1f)", g, gMax);
	}

	/**
	 * 
	 * @see com.geeksville.info.InfoField#getUnits()
	 */
	@Override
	public String getUnits() {
		return context.getString(R.string.g_meter_caption);
	}

	/**
	 * @see com.geeksville.info.InfoField#onCreate(android.app.Activity)
	 */
	@Override
	public void onCreate(Activity context) {
		super.onCreate(context);

		if (context != null) {

			// FIXME - we should share one accel client object
			accel = AccelerometerClient.create(context);
		}
	}

	/**
	 * @see com.geeksville.info.InfoField#onHidden()
	 */
	@Override
	void onHidden() {
		super.onHidden();

		if (accel != null)
			accel.deleteObserver(this);
	}

	/**
	 * @see com.geeksville.info.InfoField#onShown()
	 */
	@Override
	void onShown() {
		super.onShown();

		if (accel != null)
			accel.addObserver(this);
	}

	boolean isStarting = true;
	int startCycles = 0;

	@Override
	public void update(Observable observable, Object data) {

		// convert from m/sec to g's
		float newg = ((Float) data) / 9.6f;

		newg = Math.abs(newg - g);
		if (newg != g) {
			g = newg;
			if (!isStarting)
				gMax = Math.max(g, gMax);
			else if (startCycles <= 10)
				startCycles++;
			else
				isStarting = false;
			onChanged();
		}
	}

}
