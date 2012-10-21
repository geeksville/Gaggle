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

import org.andnav.osm.views.overlay.OpenStreetMapViewOverlayItem;

import android.app.Activity;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.widget.TextView;
import android.widget.Toast;

import com.geeksville.gaggle.R;
import com.geeksville.info.Units;
import com.geeksville.location.ExtendedWaypoint;
import com.geeksville.view.CaptionedDrawable;

public class WaypointItem extends OpenStreetMapViewOverlayItem {

	private static final String TAG = "WaypointItem";

	private ExtendedWaypoint w;
	private CaptionedDrawable marker;
	private int width, height;

	/**
	 * The actual wrapped icon we are currently displaying
	 */
	Drawable curIcon;

	public WaypointItem(ExtendedWaypoint w, Paint captionPaint) {
		super(w.name, "snippet", w.geoPoint);

		this.w = w;
		curIcon = w.getIcon();
		// Note: be careful to call mutate here, because we'll be moving each
		// drawable independently
		marker = new CaptionedDrawable(curIcon,
				captionPaint,
				w.name);

		setMarker(marker);
		setMarkerHotspotPlace(HotspotPlace.BOTTOM_CENTER);

		width = marker.getIntrinsicWidth();
		height = marker.getIntrinsicHeight();
	}

	/**
	 * Perhaps update our icon
	 * 
	 * @return true if the icon changed
	 */
	boolean updateIcon() {
		Drawable newIcon = w.getIcon();

		if (curIcon != newIcon) {
			curIcon = newIcon;
			marker.setDrawable(newIcon);
			return true;
		}

		return false;
	}

	public void handleTap(Activity context) {
		String msg = String.format(context.getString(R.string.distance_s_s_glide_s), Units.instance
				.metersToDistance(w.distanceFromPilotX), Units.instance.getDistanceUnits(), w
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
