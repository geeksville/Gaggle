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
	
	public AirspaceLoader(MapView mapView, PolygonOverlay polys){
		this.mapView = mapView;
		this.mPolys = polys;
	}
	
	@Override
	protected void onPreExecute(){
		screenBbox = mapView.getProjection().getBoundingBox();
	}

	@Override
	protected ArrayList<GeoPolygon> doInBackground(Void... params) {
		AirspaceClient ac = new AirspaceClient();
		final ArrayList<GeoPolygon> ret = ac.getAirspaces(screenBbox.getLatNorthE6()/1E6, screenBbox.getLonWestE6()/1E6,
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
