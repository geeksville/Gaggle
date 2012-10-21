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
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.geeksville.location.LocationList;
import com.geeksville.util.MathUtil;
import com.geeksville.view.ViewUtil;

/**
 * Draw an altitude plot
 * 
 * @author kevinh
 * 
 *         FIXME: On tap, change horizontal scale
 */
public class AltitudeView extends View {

	protected LocationList locs = new LocationList();
	private Paint trackPaint = new Paint(), backgroundPaint = new Paint();
	protected Paint labelPaint = new Paint();

	private int maxY, minY;

	public AltitudeView(Context context) {
		super(context);
		init();
	}

	public AltitudeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public AltitudeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		trackPaint.setColor(Color.GREEN);
		trackPaint.setStyle(Style.STROKE);
		trackPaint.setStrokeWidth(2);

		labelPaint.setColor(Color.WHITE);
		labelPaint.setTextAlign(Align.CENTER);
		labelPaint.setTextSize(32.0f);

		backgroundPaint = new Paint();
		backgroundPaint.setARGB(128, 64, 64, 128); // lt gray translucent
		backgroundPaint.setAntiAlias(true);

		generatePoints();
	}

	/**
	 * Reset our min/max scale
	 */
	private void resetScaling() {
		maxY = MathUtil.max(locs.altitudeMM.toUnsafeArray(), locs.altitudeMM.length());
		minY = MathUtil.min(locs.altitudeMM.toUnsafeArray(), locs.altitudeMM.length());
	}

	/**
	 * Low level line segments for fast redrawing
	 */
	private float[] points = new float[4];
	private int numLinesPoints;
	private int oldNumPoints;

	protected static final int border = 5;

	/**
	 * Get the lowest time we are willing to plot
	 * 
	 * @return
	 */
	protected int getMinX() {
		// int[] times = locs.timeMsec.toUnsafeArray();
		// int numpoints = locs.numPoints();

		if (ViewUtil.isDesignTime(getContext()))
			return 0;

		return locs.timeMsec.get(0); // Plot everything
	}

	private void generatePoints() {
		int numpoints = locs.numPoints();

		int[] alts = locs.altitudeMM.toUnsafeArray();
		int[] times = locs.timeMsec.toUnsafeArray();

		if (ViewUtil.isDesignTime(getContext())) {
			alts = new int[] { 1, 10, 30, 52, 25, 7 };
			times = new int[] { 0, 20, 30, 40, 50, 60 };
			numpoints = alts.length;
			resetScaling();
		}

		// Could anything have possibly changed?
		if (numpoints == oldNumPoints)
			return;

		// Make sure we have enough space for all our segments
		int needed = (numpoints - 1) * 4;
		if (points.length < needed)
			points = new float[2 * needed];

		int maxX = times[numpoints - 1];
		int minX = getMinX();

		int height = getHeight();
		int width = getWidth() - 1;
		int contentY = height - (2 * border);
		int contentX = width - (2 * border);
		int prevX = -1, prevY = 0;

		// Generate a completely new set of line segments (because if we add
		// even one point everything gets shifted over)
		numLinesPoints = 0;
		oldNumPoints = 0;

		for (int i = oldNumPoints; i < numpoints; i++) {
			int time = times[i];

			// We only consider points newer than our min start time
			if (time >= minX) {
				int alt = alts[i];

				minY = Math.min(alt, minY);
				maxY = Math.max(alt, maxY);

				// Until we have a change in Y no point drawing altitude (and
				// prevent div by zero)
				if (maxY != minY) {
					// Flip Y so that higher values are towards the top
					int ypos = border + contentY - contentY * (alt - minY) / (maxY - minY);

					int xpos = (int) (border + contentX * ((long) time - minX) / (maxX - minX));

					if (prevX != -1) {
						points[numLinesPoints++] = prevX;
						points[numLinesPoints++] = prevY;
						points[numLinesPoints++] = xpos;
						points[numLinesPoints++] = ypos;
					}

					prevX = xpos;
					prevY = ypos;
				}
			}
		}

		oldNumPoints = numpoints;
	}

	protected void handleTap() {
		// Do nothing
	}

	protected void drawLabels(Canvas canvas) {
	}

	@Override
	protected void onDraw(Canvas canvas) {

		drawBackground(canvas);

		drawLabels(canvas);

		generatePoints(); // Perhaps regenerate line segments
		if (numLinesPoints == 0)
			return;

		canvas.drawLines(points, 0, numLinesPoints, trackPaint);

	}

	private void drawBackground(Canvas canvas) {
		// Draw our grey background
		RectF drawRect = new RectF();
		drawRect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());

		canvas.drawRoundRect(drawRect, 5, 5, backgroundPaint);
	}

	/**
	 * @param locs
	 *            the locs to set
	 */
	public void setLocs(LocationList locs) {
		this.locs = locs;

		resetScaling();
	}

	/**
	 * When tapped we cycle between time scales
	 * 
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// The user just tapped on us
			handleTap();
		}

		return super.onTouchEvent(event);
	}

}
