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
