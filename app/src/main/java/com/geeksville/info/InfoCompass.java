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
