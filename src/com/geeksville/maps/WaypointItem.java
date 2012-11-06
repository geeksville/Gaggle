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


import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.widget.TextView;
import android.widget.Toast;

import com.geeksville.gaggle.R;
import com.geeksville.info.Units;
import com.geeksville.location.ExtendedWaypoint;
import com.geeksville.view.CaptionedDrawable;

public class WaypointItem extends ExtendedOverlayItem {

	private static final String TAG = "WaypointItem";

	private ExtendedWaypoint extendedWpt;

	public ExtendedWaypoint getExtendedWpt() {
		return extendedWpt;
	}

	private CaptionedDrawable marker;
	private int width, height;

	/**
	 * The actual wrapped icon we are currently displaying
	 */
	Drawable curIcon;

	public WaypointItem(ExtendedWaypoint w, Paint captionPaint, Context context) {
		super(w.name, w.description, w.geoPoint, context);
		this.extendedWpt = w;
		curIcon = w.getIcon();
		// Note: be careful to call mutate here, because we'll be moving each
		// drawable independently
		marker = new CaptionedDrawable(curIcon,
				captionPaint,
				w.name);

		setMarker(marker);
		setMarkerHotspot(HotspotPlace.BOTTOM_CENTER);

		width = marker.getIntrinsicWidth();
		height = marker.getIntrinsicHeight();
	}

	/**
	 * Perhaps update our icon
	 * 
	 * @return true if the icon changed
	 */
	boolean updateIcon() {
		final Drawable newIcon = extendedWpt.getIcon();
		boolean refresh=false;
		
		if (curIcon != newIcon) {
			curIcon = newIcon;
			marker.setDrawable(newIcon);
			refresh = true;
		}
		

		return refresh;
	}

	public void handleTap(Activity context) {
		String msg = String.format(context.getString(R.string.distance_s_s_glide_s), Units.instance
				.metersToDistance(extendedWpt.distanceFromPilotX), Units.instance.getDistanceUnits(), extendedWpt
				.glideRatioString());

		Toast t = Toast.makeText(context, msg, Toast.LENGTH_LONG);
		TextView v = (TextView) t.getView().findViewById(android.R.id.message);
		v.setGravity(0x11); // gravity=center
		t.show();
	}

	/**
	 * Show human readable for debugging
	 */
	@Override
	public String toString() {
		return String.format("WP:%s(lat %d, long %d)", getTitle(), mGeoPoint.getLatitudeE6(),
				mGeoPoint.getLongitudeE6());
	}
}
