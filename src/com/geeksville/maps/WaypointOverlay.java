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

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.overlay.OpenStreetMapViewItemizedOverlay;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;

import com.geeksville.gaggle.GaggleApplication;
import com.geeksville.location.ExtendedWaypoint;
import com.geeksville.location.WaypointCursor;
import com.geeksville.location.WaypointDB;

public class WaypointOverlay extends OpenStreetMapViewItemizedOverlay<WaypointItem> implements
		Observer {

	private final Paint captionPaint = new TextPaint();

	private WaypointDB db;
	private WaypointCursor cursor;
	private OpenStreetMapView view;
	private Activity context;

	public WaypointOverlay(Activity context, OpenStreetMapView view) {
		// per example, we want the bounds to be centered just below this
		// drawable. We use a alpha channel to not obscure terrain too much...
		// super(boundCenterBottom(context.getResources().getDrawable(R.drawable.blue)));
		super(context, new ArrayList<WaypointItem>(), null);

		this.context = context;
		this.view = view;
		captionPaint.setTextSize(captionPaint.getTextSize() + 3); // A bit
		// bigger
		// than the
		// default
		captionPaint.setTextAlign(Align.CENTER);
		captionPaint.setColor(Color.WHITE);
		captionPaint.setShadowLayer(3, 1, 1, Color.BLACK);

		// dimWaypoint = boundCenterBottom(context.getResources().getDrawable(
		// R.drawable.blue).mutate());
		// dimWaypoint.setAlpha(192);

		// FIXME, close the backing DB when the waypoint cache is done with it
		db = ((GaggleApplication) context.getApplication()).getWaypoints();

		fillFromDB(context);
	}

	public static Drawable boundCenterBottom(Drawable d) {
		return d;
		// busted on OSM FIXME
		// return ItemizedOverlay.boundCenterBottom(d);
	}

	public static Drawable boundCenter(Drawable d) {
		return d;
		// return ItemizedOverlay.boundCenter(d);
	}

	public void onPause() {
		db.deleteObserver(this);
	}

	public void onResume() {
		db.addObserver(this);
	}

	/**
	 * Load our waypoints from the db (move this elsewhere?)
	 */
	private void fillFromDB(Activity context) {

		cursor = db.fetchWaypointsByDistance();

		mItemList.clear();
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			ExtendedWaypoint w = cursor.getWaypoint();
			WaypointItem item = new WaypointItem(w, captionPaint);
			mItemList.add(item);
		}

		// FIXME, handle the addition of waypoints after the map is already up
		// populate();
	}

	// @Override
	// public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long
	// when) {
	// // We skip the shadow draws, because they make our waypoint captions
	// // look goofy
	// if (!shadow)
	// return super.draw(canvas, mapView, shadow, when);
	// else
	// return false;
	// }

	@Override
	public void update(Observable observable, Object data) {
		// Our waypoints may have changed, refresh our WaypointItems
		// we just change the
		// contents of the CaptionedDrawables
		// and then invalidate only if needed

		boolean needRedraw = false;
		for (int i = 0; i < mItemList.size(); i++) {
			WaypointItem item = mItemList.get(i);

			needRedraw |= item.updateIcon();
		}

		if (needRedraw)
			view.postInvalidate();
	}

	@Override
	protected boolean onTap(int i) {
		WaypointItem item = mItemList.get(i);

		// first click - just highlight and show basic info
		// if (!item.equals(getFocus())) {
		view.getController().setCenter(item.mGeoPoint);
		// setFocus(item);
		item.handleTap(context);
		// }

		return true;
	}

}
