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

import org.andnav.osm.views.OpenStreetMapView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import com.geeksville.maps.CenteredMyLocationOverlay;
import com.geeksville.maps.WaypointOverlay;

/**
 * Shows the users heading instead of the standard blue dot
 * 
 * @author kevinh
 * 
 */
public class ShowHeadingOverlay extends CenteredMyLocationOverlay {

	private float bearing;

	private Drawable icon;

	public ShowHeadingOverlay(Context context, OpenStreetMapView mapView) {
		super(context, mapView);

		icon = WaypointOverlay.boundCenter(context.getResources()
				.getDrawable(R.drawable.in_flight /*
												 * R. drawable . arrow
												 */));

		// Put center in the middle of the drawable
		// int w = icon.getIntrinsicWidth(), h = icon.getIntrinsicHeight();
		// icon.setBounds(-w / 2, -h / 2, w, h);
		// icon.setBounds(0, 0, w, h);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.geeksville.maps.CenteredMyLocationOverlay#drawMyLocation(android.
	 * graphics.Canvas, android.location.Location, long)
	 */
	// @Override
	public void fixmeBustedOnOSM(final Canvas canvas, final OpenStreetMapView mapView) {
		// protected void drawMyLocation(Canvas canvas, OpenStreetMapView
		// mapView, Location lastFix,
		// GeoPoint myLocation, long when) {

		// super.drawMyLocation(canvas, mapView, lastFix, myLocation, when);
		super.keepCentered(mapView, getMyLocation());

		int saveCount = canvas.save();
		canvas.translate(curPixelPos.x, curPixelPos.y);
		// icon.setRotation((int) -bearing);
		canvas.rotate(-bearing);
		icon.draw(canvas);
		canvas.restoreToCount(saveCount);
	}

	/**
	 * FIXME - cache the bearing someplace earlier
	 * 
	 * 
	 */
	// @Override
	protected void drawCompass(Canvas canvas, float bearing) {
		this.bearing = bearing;

		// super.drawCompass(canvas, bearing);
	}

}
