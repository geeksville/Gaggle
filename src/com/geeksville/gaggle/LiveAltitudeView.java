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
package com.geeksville.gaggle;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

/**
 * An alitude view that is showing live in flight data
 * 
 * @author kevinh
 * 
 */
public class LiveAltitudeView extends AltitudeView {
	public LiveAltitudeView(Context context) {
		super(context);
	}

	public LiveAltitudeView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LiveAltitudeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	private static final String[] timescaleLabels = { "Plot: All", "Plot: 5 min",
			"Plot: 20 min", "Plot: 1 hour" };
	private static final int[] timescaleSecs = { -1, 5 * 60, 20 * 60, 60 * 60 };

	private int curTimescale = 0;

	/**
	 * Get the lowest time we are willing to plot
	 * 
	 * @return
	 */
	@Override
	protected int getMinX() {
		int[] times = locs.timeMsec.toUnsafeArray();
		int numpoints = locs.numPoints();
		int maxX = times[numpoints - 1];
		int minX = times[0];

		int tscaleSecs = timescaleSecs[curTimescale];
		if (tscaleSecs >= 0) {
			int newMinX = maxX - tscaleSecs * 1000;
			minX = Math.max(minX, newMinX);
		}

		return minX;
	}

	@Override
	protected void handleTap() {
		curTimescale = (curTimescale + 1) % timescaleLabels.length;
		invalidate();
	}

	@Override
	protected void drawLabels(Canvas canvas) {
		super.drawLabels(canvas);

		int height = getHeight();
		int width = getWidth();
		canvas.drawText(timescaleLabels[curTimescale], (width / 2), border + 23,
				labelPaint);
	}
}
