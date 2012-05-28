package com.geeksville.airspace;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;

import android.os.AsyncTask;
import android.util.Log;

import com.geeksville.maps.PolygonOverlay;
import com.geeksville.maps.ScrollChangeMapView;

public class AirspaceScrollListener implements ScrollChangeMapView.OnChangeListener {
	private AirspaceLoader previous;
	private final PolygonOverlay mPolys;
	
	public AirspaceScrollListener(PolygonOverlay polys){
		mPolys = polys;
	}
	
	@Override
	public void onChange(MapView view, IGeoPoint newCenter,
			IGeoPoint oldCenter, int newZoom, int oldZoom) {
		if (previous != null){
			if (previous.getStatus().equals(AsyncTask.Status.PENDING) ||
					previous.getStatus().equals(AsyncTask.Status.RUNNING)){
				Log.d("AirspaceScroll", "canceled");
				previous.cancel(true);
			}
		}
		Log.d("AirspaceScroll", "Executing");
		previous = new AirspaceLoader(view, mPolys);
		previous.execute();
	}
}
