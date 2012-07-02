package com.geeksville.weather;

import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

import com.geeksville.gaggle.R;

import android.content.Context;
import android.graphics.drawable.Drawable;

public abstract class Station extends ExtendedOverlayItem implements Stationable {

	final protected GeoPoint location;
	final protected String name;

	public Station(String aTitle, String aDescription, GeoPoint aGeoPoint,
			Context context) {
		super(aTitle, aDescription, aGeoPoint, context);
		Drawable marker = context.getResources().getDrawable(R.drawable.marker_node);
		setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
		setMarker(marker);
		location = aGeoPoint;
		name = aTitle;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getAltitude(){
		return location.getAltitude();
	}

	@Override
	public final GeoPoint getLocation() {
		return location;
	}
}
