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
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Draws a text caption underneath the specified drawable
 * 
 * @author kevinh
 * 
 */
public class CaptionedDrawable extends DrawableWrapper {

	String caption;
	Paint paint;
	Rect captionBounds = new Rect();

	public CaptionedDrawable(Drawable drawable, Paint captionPaint, String caption) {
		super(drawable);

		paint = captionPaint;
		paint.getTextBounds(caption, 0, caption.length(), captionBounds);
		this.caption = caption;
	}

	/**
	 * Set the drawable we are wrapping
	 * 
	 * @param wrapped
	 */
	public void setDrawable(Drawable wrapped) {
		// If we change the drawable make sure to copy in the correct bounds
		drawable = wrapped;
		// drawable.setBounds(copyBounds());
	}

	public Drawable getDrawable() {
		return drawable;
	}

	@Override
	public void draw(Canvas canvas) {
		Rect bounds = drawable.getBounds();

		drawable.draw(canvas);

		float x = (bounds.left + bounds.right) / 2;
		float y = bounds.bottom + captionBounds.height();

		// Draw our caption centered underneath
		canvas.drawText(caption, x, y, paint);
	}

}
