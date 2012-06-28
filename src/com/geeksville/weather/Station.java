package com.geeksville.weather;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

public abstract class Station extends OverlayItem implements Stationable {

	public Station(String aUid, String aTitle, String aDescription,
			GeoPoint aGeoPoint) {
		super(aUid, aTitle, aDescription, aGeoPoint);
	}

	public Station(String aTitle, String aDescription,
			GeoPoint aGeoPoint) {
		super(aTitle, aDescription, aGeoPoint);
	}
}
