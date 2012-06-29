package com.geeksville.weather;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

public abstract class Station extends OverlayItem implements Stationable {
	final protected GeoPoint location;
	final protected String name;

	public Station(String aUid, String aTitle, String aDescription,
			GeoPoint aGeoPoint) {
		super(aUid, aTitle, aDescription, aGeoPoint);
		location = aGeoPoint;
		name = aTitle;
	}

	public Station(String aTitle, String aDescription,
			GeoPoint aGeoPoint) {
		super(aTitle, aDescription, aGeoPoint);
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
