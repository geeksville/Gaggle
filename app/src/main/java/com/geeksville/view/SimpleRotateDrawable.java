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
package com.geeksville.view;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Wraps a drawable to simply rotate it about the center
 * 
 * @author kevinh
 * 
 *         The standard Android RotateDrawable only handles animations of values
 *         passed in by XML.
 */
public class SimpleRotateDrawable extends DrawableWrapper {

	private int degrees;

	public SimpleRotateDrawable(Drawable drawable) {
		super(drawable);
	}

	public void setRotation(int degrees) {
		this.degrees = degrees;
	}

	@Override
	public void draw(Canvas canvas) {
		int saveCount = canvas.save();

		Rect bounds = drawable.getBounds();

		int w = bounds.right - bounds.left;
		int h = bounds.bottom - bounds.top;

		// float px = st.mPivotXRel ? (w * st.mPivotX) : st.mPivotX;
		// float py = st.mPivotYRel ? (h * st.mPivotY) : st.mPivotY;
		float px = (float) (w / 2.0);
		float py = (float) (h / 2.0);

		canvas.rotate(degrees, px, py);

		drawable.draw(canvas);

		canvas.restoreToCount(saveCount);
	}
}
