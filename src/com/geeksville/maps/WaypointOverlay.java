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

import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MotionEvent;

import com.geeksville.gaggle.GaggleApplication;
import com.geeksville.gaggle.R;
import com.geeksville.location.ExtendedWaypoint;
import com.geeksville.location.Waypoint;
import com.geeksville.location.WaypointCursor;
import com.geeksville.location.WaypointDB;

public class WaypointOverlay extends ItemizedOverlayWithBubble<WaypointItem>
		implements
		Observer {

	private static final String TAG = "WaypointOverlay";

	private final Paint captionPaint = new TextPaint();

	private WaypointDB db;
	private WaypointCursor cursor;
	private MapView view;
	private Context context;

	public WaypointOverlay(final Activity context, final MapView view) {
		// per example, we want the bounds to be centered just below this
		// drawable. We use a alpha channel to not obscure terrain too much...
		// super(boundCenterBottom(context.getResources().getDrawable(R.drawable.blue)));
//		super(new ArrayList<WaypointItem>(), context.getResources()
//				.getDrawable(R.drawable.flag), new OnItemGestureListener<WaypointItem>() {
//
//					@Override
//					public boolean onItemLongPress(int arg0, WaypointItem arg1) {
//						// TODO Auto-generated method stub
//						return false;
//					}
//
//					@Override
//					public boolean onItemSingleTapUp(int index, WaypointItem item) {
//						// TODO Auto-generated method stub
//						view.getController().animateTo(item.mGeoPoint);
//						item.handleTap(context);
//						return true;
//					}
//				}, new DefaultResourceProxyImpl(context));
		super(context, new ArrayList<WaypointItem>(), view);

//		mItemList = new ArrayList<WaypointItem>();

		this.view = view;
		captionPaint.setTextSize(captionPaint.getTextSize() + 3); 
		// A bit bigger than the default
		captionPaint.setTextAlign(Align.CENTER);
		captionPaint.setColor(Color.WHITE);
		captionPaint.setShadowLayer(3, 1, 1, Color.BLACK);

		// dimWaypoint = boundCenterBottom(context.getResources().getDrawable(
		// R.drawable.blue).mutate());
		// dimWaypoint.setAlpha(192);

		// FIXME, close the backing DB when the waypoint cache is done with it
		db = ((GaggleApplication) context.getApplication()).getWaypoints();

		this.context = context.getApplicationContext();
		fillFromDB(context);
	}

	@Override
	public boolean onLongPress(MotionEvent event, MapView mapView) {
		// Adapted from
		// https://groups.google.com/forum/?fromgroups#!topic/osmdroid/HQrwVil-W6w
		if (!super.onLongPress(event, mapView)) {
			/* So we did not get an icon */
			final Projection pj = mapView.getProjection();
			final int eventX = (int) event.getX();
			final int eventY = (int) event.getY();

			GeoPoint mGeoPoint = (GeoPoint) pj.fromPixels(eventX, eventY);

			java.util.Date now = new java.util.Date();
			String name = DateFormat.format("yy/MM/dd kk:mm:ss", now)
					.toString();

			ExtendedWaypoint w = new ExtendedWaypoint(name,
					mGeoPoint.getLatitudeE6()/1E6, mGeoPoint.getLongitudeE6()/1E6,
					mGeoPoint.getAltitude(), 0,
					Waypoint.Type.Unknown.ordinal());
			db.add(w);
			WaypointItem item = new WaypointItem(w, captionPaint, context);
			addItem(item);
			populate();
			view.postInvalidate();
			return true;
		}
		return false;
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
		removeAllItems(false);
		
		//mItemList.clear();
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToPosition(i);
			ExtendedWaypoint w = cursor.getWaypoint();
			WaypointItem item = new WaypointItem(w, captionPaint, context);
			addItem(item);
		}

		// FIXME, handle the addition of waypoints after the map is already up
		populate();
		view.postInvalidate();
	}

	@Override
	public void update(Observable observable, Object data) {
//		// Our waypoints may have changed, refresh our WaypointItems
//		// we just change the
//		// contents of the CaptionedDrawables
//		// and then invalidate only if needed
		boolean needRedraw = false;
		for (WaypointItem item : mItemList) {
			needRedraw |= item.updateIcon();
		}

		if (needRedraw)
			view.postInvalidate();
	}
	
	
//
//	@Override
	// FIXME where has this been moved to ?
//	protected boolean onTap(int i) {
//		WaypointItem item = mItemList.get(i);
//
//		// first click - just highlight and show basic info
//		// if (!item.equals(getFocus())) {
//		view.getController().animateTo(item.mGeoPoint);
//		// setFocus(item);
//		item.handleTap(context);
//		// }
//
//		return true;
//	}

//	@Override
//	public boolean onSnapToItem(int arg0, int arg1, Point arg2, IMapView arg3) {
//		// TODO Auto-generated method stub
//		return false;
//	}

//	@Override
//	protected WaypointItem createItem(int arg0) {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	@Override
//	public int size() {
//		return mItemList.size();
//	}

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
