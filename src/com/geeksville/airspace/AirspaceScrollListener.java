package com.geeksville.airspace;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.geeksville.maps.PolygonOverlay;
import com.geeksville.maps.ScrollChangeMapView;

public class AirspaceScrollListener implements ScrollChangeMapView.OnChangeListener {
	private AirspaceLoader previous;
	private final PolygonOverlay mPolys;
	private String host;
	
	public void setHost(String host) {
		this.host = host;
	}

	public AirspaceScrollListener(PolygonOverlay polys, String host){
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
		previous = new AirspaceLoader(view, mPolys, host);
		previous.execute();
	}
}
