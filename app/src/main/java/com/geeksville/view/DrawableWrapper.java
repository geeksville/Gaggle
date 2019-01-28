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
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Wraps a drawable, allowing you to override the draw behavior
 * 
 * @author kevinh
 */
public abstract class DrawableWrapper extends Drawable {

	protected Drawable drawable;

	public DrawableWrapper(Drawable drawable) {
		this.drawable = drawable;

		// Pull our initial bounds from the object we are wrapping
		super.setBounds(drawable.copyBounds());
	}

	@Override
	public int getOpacity() {
		return drawable.getOpacity();
	}

	@Override
	public void setAlpha(int alpha) {
		drawable.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		drawable.setColorFilter(cf);
	}

	// @Override
	// protected void onBoundsChange(Rect bounds) {
	// drawable.setBounds(bounds.left, bounds.top, bounds.right, bounds.bottom);
	// }

	@Override
	public int getIntrinsicWidth() {
		return drawable.getIntrinsicWidth();
	}

	@Override
	public int getIntrinsicHeight() {
		return drawable.getIntrinsicHeight();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#draw(android.graphics.Canvas)
	 */
	@Override
	public void draw(Canvas canvas) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#setBounds(int, int, int, int)
	 */
	@Override
	public void setBounds(int left, int top, int right, int bottom) {
		// TODO Auto-generated method stub
		super.setBounds(left, top, right, bottom);
		drawable.setBounds(left, top, right, bottom);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.graphics.drawable.Drawable#setBounds(android.graphics.Rect)
	 */
	@Override
	public void setBounds(Rect bounds) {
		// TODO Auto-generated method stub
		super.setBounds(bounds);
		drawable.setBounds(bounds);
	}

}
