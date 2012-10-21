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
package com.geeksville.maps;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import org.andnav.osm.DefaultResourceProxyImpl;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.overlay.OpenStreetMapViewItemizedOverlay;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;

import com.geeksville.gaggle.GaggleApplication;
import com.geeksville.gaggle.R;
import com.geeksville.location.ExtendedWaypoint;
import com.geeksville.location.WaypointCursor;
import com.geeksville.location.WaypointDB;

public class WaypointOverlay extends OpenStreetMapViewItemizedOverlay<WaypointItem>
		implements
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
		super(context, new ArrayList<WaypointItem>(), context.getResources().getDrawable(
				R.drawable.flag), null, null, new DefaultResourceProxyImpl(context));

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

	/**
	 * Reset the bounds so the center of the drawable is at zero
	 * 
	 * @param d
	 * @return
	 */
	public static Drawable boundCenterBottom(Drawable d) {
		// d = d.mutate();

		int width = d.getIntrinsicWidth();
		int height = d.getIntrinsicHeight();

		Rect r = new Rect();
		r.left = -width / 2;
		r.top = -height;
		r.right = r.left + width;
		r.bottom = 0;
		d.setBounds(r);

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
		view.getController().animateTo(item.mGeoPoint);
		// setFocus(item);
		item.handleTap(context);
		// }

		return true;
	}

	// /**
	// * Overriden for debugging
	// */
	// @Override
	// protected void onDrawItem(Canvas c, int index, Point curScreenCoords) {
	// super.onDrawItem(c, index, curScreenCoords);
	//
	// // WaypointItem wp = mItemList.get(index);
	// // Log.d("WaypointOverlay", String.format("%s screen=%d,%d", wp,
	// // curScreenCoords.x, curScreenCoords.y));
	// }

}
