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

import android.app.Activity;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.widget.TextView;
import android.widget.Toast;

import com.geeksville.info.Units;
import com.geeksville.location.ExtendedWaypoint;
import com.geeksville.view.CaptionedDrawable;
import com.google.android.maps.OverlayItem;

public class WaypointItem extends OverlayItem {

	ExtendedWaypoint w;
	CaptionedDrawable marker;

	/**
	 * The actual wrapped icon we are currently displaying
	 */
	Drawable curIcon;

	public WaypointItem(ExtendedWaypoint w, Paint captionPaint) {
		super(w.geoPoint, w.name, "snippet");

		this.w = w;
		curIcon = w.getIcon();
		marker = new CaptionedDrawable(WaypointOverlay.boundCenterBottom(curIcon), captionPaint,
				w.name);
		setMarker(marker);
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
			marker.setDrawable(WaypointOverlay.boundCenterBottom(newIcon));
			return true;
		}

		return false;
	}

	public void handleTap(Activity context) {
		String msg = String.format("Distance: %s %s\nGlide: %s:1", Units.instance
				.metersToDistance(w.distanceFromPilotX), Units.instance.getDistanceUnits(), w
				.glideRatioString());

		Toast t = Toast.makeText(context, msg, Toast.LENGTH_LONG);
		TextView v = (TextView) t.getView().findViewById(android.R.id.message);
		v.setGravity(0x11); // gravity=center
		t.show();
	}
}
