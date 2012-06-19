package com.geeksville.airspace;

import java.util.Timer;
import java.util.TimerTask;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.views.MapView;
import android.os.AsyncTask;
import android.util.Log;
import com.geeksville.maps.PolygonOverlay;

//public class AirspaceScrollListener implements ScrollChangeMapView.OnChangeListener {
public class AirspaceScrollListener implements MapListener {

	private long mEventsTimeout = 250L; // Set this variable to your preferred timeout
	private IGeoPoint mLastCenterPosition;
	private int mLastZoomLevel;
	private Timer mChangeDelayTimer = new Timer();
	
	private AirspaceLoader previous;
	private final PolygonOverlay mPolys;
	private final MapView mapView;
	private String host;
	
	public void setHost(String host) {
		this.host = host;
	}

	public AirspaceScrollListener(MapView mapView, PolygonOverlay polys, String host){
		this.mPolys = polys;
		this.host = host;
		this.mapView = mapView;
		mLastCenterPosition = mapView.getMapCenter();
		mLastZoomLevel = mapView.getZoomLevel();
	}


	public void update() {
		if (previous != null){
			if (previous.getStatus().equals(AsyncTask.Status.PENDING) ||
					previous.getStatus().equals(AsyncTask.Status.RUNNING)){
				Log.d("AirspaceScroll", "canceled");
				previous.cancel(true);
			}
		}
		Log.d("AirspaceScroll", "Executing");
		previous = new AirspaceLoader(mapView, mPolys, host);
		previous.execute();
	}

	@Override
	public boolean onScroll(ScrollEvent scroll) {
		if (mapView.getMapCenter().equals(mLastCenterPosition)){
			resetMapChangeTimer();
		}
		return true;
	}

	@Override
	public boolean onZoom(ZoomEvent zoom) {
		if (zoom.getZoomLevel() != mLastZoomLevel){
			resetMapChangeTimer();
		}
		return true;
	}

	private void resetMapChangeTimer()
	{
		mChangeDelayTimer.cancel();
		mChangeDelayTimer = new Timer();
		mChangeDelayTimer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				update();
				mLastCenterPosition = mapView.getMapCenter();
				mLastZoomLevel = mapView.getZoomLevel();
			}
		}, mEventsTimeout);
	}
}
