package com.geeksville.airspace;

import java.util.ArrayList;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;

import android.os.AsyncTask;

import com.geeksville.maps.PolygonOverlay;
import com.geeksville.maps.PolygonOverlay.GeoPolygon;

public class AirspaceLoader extends AsyncTask<Void, Void, ArrayList<GeoPolygon>> {
	private BoundingBoxE6 screenBbox;
	private final MapView mapView;
	private final PolygonOverlay mPolys;
	private AirspaceClient airspaceclient;
	
	public AirspaceLoader(MapView mapView, PolygonOverlay polys, 
			final String host, final String[] classes, final int maxfloor){
		this.mapView = mapView;
		this.mPolys = polys;
		airspaceclient = new AirspaceClient(host, classes, maxfloor);
	}
	
	@Override
	protected void onPreExecute(){
		screenBbox = mapView.getProjection().getBoundingBox();
	}

	@Override
	protected ArrayList<GeoPolygon> doInBackground(Void... params) {
		final ArrayList<GeoPolygon> ret = airspaceclient.getAirspaces(screenBbox.getLatNorthE6()/1E6, screenBbox.getLonWestE6()/1E6,
				screenBbox.getLatSouthE6()/1E6, screenBbox.getLonEastE6()/1E6);
		return ret;
	}

	@Override
	protected void onPostExecute(ArrayList<GeoPolygon> polygons){
		mPolys.clearPolys();
		for (GeoPolygon poly: polygons){
			mPolys.addPolygon(poly);
		}
		mapView.invalidate();
	}
}
