/****************************************************************************************
 * Gaggle is Copyright 2010, 2011, and 2012 by Kevin Hester of Geeksville Industries LLC,
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

		init();
	}

	public LiveAltitudeView(Context context, AttributeSet attrs) {
		super(context, attrs);

		init();
	}

	public LiveAltitudeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init();
	}

	private String[] timescaleLabels;
	private static final int[] timescaleSecs = { -1, 5 * 60, 20 * 60, 60 * 60 };

	private int curTimescale = 0;

	private void init() {
		timescaleLabels = new String[] {
				getContext().getString(R.string.plot_all),
				getContext().getString(R.string.plot_5_min),
				getContext().getString(R.string.plot_20_min),
				getContext().getString(R.string.plot_1_hour) };
	}

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
