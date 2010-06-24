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

import android.content.Context;
import android.graphics.Canvas;

import com.geeksville.gaggle.R;
import com.geeksville.location.CompassClient;
import com.geeksville.view.SimpleRotateDrawable;

/**
 * A compass arrow that corrects for the current orientation of the phone
 * 
 * @author kevinh
 * 
 */
public class HeadingDrawable extends SimpleRotateDrawable {

	private CompassClient compass;

	private int heading;

	public HeadingDrawable(Context context, CompassClient compass) {
		super(context.getResources().getDrawable(R.drawable.arrow));

		this.compass = compass;
	}

	/**
	 * A magnetic heading that this arrow should be showing
	 * 
	 * @param degrees
	 */
	public void setHeading(int degrees) {

		heading = degrees;
	}

	/**
	 * The correct orientation of the image depends both on the heading and the
	 * current compass reading
	 */
	@Override
	public void draw(Canvas canvas) {

		setRotation(-heading - compass.bearing);

		super.draw(canvas);
	}
}
