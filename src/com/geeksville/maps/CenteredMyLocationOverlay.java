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
package com.geeksville.maps;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.MyLocationOverlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Location;
import android.view.View;

/**
 * show the user's position, but always centered on the screen
 * 
 * @author kevinh
 * 
 */
public class CenteredMyLocationOverlay extends MyLocationOverlay {

	protected Point curPixelPos;

	boolean handlingGPSUpdate = false;

	public CenteredMyLocationOverlay(Context context, MapView mapView) {
		super(context, mapView);
	}

	/**
	 * Fast checking for the middle 50% (or whatever of the window)
	 */
	private static final int middleNum = 4, middleDenom = 16;

	/**
	 * Is this point in the middle 50% of the view?
	 * 
	 * @param view
	 * @param pt
	 * @return
	 */
	private static boolean inMiddle(View view, Point pt) {

		int w, min, max;
		w = view.getWidth();
		min = w * middleNum / middleDenom;
		max = w * (middleDenom - middleNum) / middleDenom;
		if (pt.x < min || pt.x > max)
			return false;

		w = view.getHeight();
		min = w * middleNum / middleDenom;
		max = w * (middleDenom - middleNum) / middleDenom;
		if (pt.y < min || pt.y > max)
			return false;

		return true;
	}

	/**
	 * 
	 * @param mapView
	 * @param myLocation
	 * 
	 *            If the user moves outside of the middle section keep em
	 *            visible. We only do this when the user first crosses this
	 *            boundary, so if the user has manually scrolled away from his
	 *            position we won't force him back. We are also careful to only
	 *            do this if we recently received a location update from the gps
	 *            - so click drags won't mess us up.
	 */
	protected void keepCentered(MapView mapView, GeoPoint myLocation) {
		// If we don't have an old position, assume it was centered, so we
		// will zoom if needed. Always update this, so that click drags work
		boolean oldInMiddle = curPixelPos != null ? inMiddle(mapView, curPixelPos) : true;
		Projection proj = mapView.getProjection();
		curPixelPos = proj.toMapPixels(myLocation, curPixelPos);

		if (handlingGPSUpdate) {
			handlingGPSUpdate = false;

			boolean newInMiddle = inMiddle(mapView, curPixelPos);
			if (oldInMiddle && !newInMiddle) {
				MapController ctrl = mapView.getController();
				ctrl.setCenter(myLocation);
			}
		}
	}

	/**
	 * Watch for GPS updates
	 * 
	 */
	@Override
	public synchronized void onLocationChanged(Location location) {

		handlingGPSUpdate = true;
		super.onLocationChanged(location);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.andnav.osm.views.overlay.MyLocationOverlay#onDraw(android.graphics
	 * .Canvas, org.andnav.osm.views.OpenStreetMapView)
	 * 
	 * If the user moves outside of the middle section keep em visible. We only
	 * do this when the user first crosses this boundary, so if the user has
	 * manually scrolled away from his position we won't force him back. We are
	 * also careful to only do this if we recently received a location update
	 * from the gps - so click drags won't mess us up.
	 */
	@Override
	public void draw(Canvas c, MapView mapView, boolean shadow) {
		GeoPoint myLoc = getMyLocation();

		if (myLoc != null)
			keepCentered(mapView, myLoc);

		super.draw(c, mapView, shadow);
	}

}
