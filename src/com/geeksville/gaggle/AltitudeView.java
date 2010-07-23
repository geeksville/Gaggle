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

import com.geeksville.location.LocationList;
import com.geeksville.util.MathUtil;
import com.geeksville.view.ViewUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

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

					int xpos = border + contentX * (time - minX) / (maxX - minX);

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
