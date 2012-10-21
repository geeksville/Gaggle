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
import android.graphics.drawable.Drawable;

import com.geeksville.gaggle.R;
import com.geeksville.location.CompassClient;

/**
 * Read the magnetic heading as measured by the compass
 * 
 * @author kevinh
 * 
 */
public class InfoCompass extends InfoField implements Observer {

	private int direction;

	private HeadingDrawable image;

	private CompassClient compass;

	@Override
	public String getLabel() {
		return context.getString(R.string.heading);
	}

	/**
	 * 
	 * @see com.geeksville.info.InfoField#getText()
	 */
	@Override
	public String getText() {
		return String.format("%d", direction);
	}

	/**
	 * 
	 * @see com.geeksville.info.InfoField#getUnits()
	 */
	@Override
	public String getUnits() {
		// Unicode for the degree symbol
		return "\u00B0";
	}

	/**
	 * @see com.geeksville.info.InfoField#onCreate(android.app.Activity)
	 */
	@Override
	public void onCreate(Activity context) {
		super.onCreate(context);

		if (context != null) {
			compass = CompassClient.create(context);

			image = new HeadingDrawable(context, compass);
		}
	}

	/**
	 * @see com.geeksville.info.InfoField#onHidden()
	 */
	@Override
	void onHidden() {
		super.onHidden();

		if (compass != null)
			compass.deleteObserver(this);
	}

	/**
	 * @see com.geeksville.info.InfoField#onShown()
	 */
	@Override
	void onShown() {
		super.onShown();

		if (compass != null)
			compass.addObserver(this);
	}

	/**
	 * Our directional arrow
	 * 
	 * @see com.geeksville.info.InfoField#getImage()
	 */
	@Override
	public Drawable getImage() {
		return image;
	}

	@Override
	public void update(Observable observable, Object data) {

		int ndirection = (Integer) data;

		if (ndirection != direction) {
			direction = ndirection;

			onChanged();
		}
	}

}
