package com.geeksville.airspace;

import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.views.MapView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.geeksville.gaggle.R;
import com.geeksville.maps.PolygonOverlay;

public class AirspaceScrollListener implements MapListener {

	private final long mEventsTimeout = 250L; // Set this variable to your preferred timeout
	private IGeoPoint mLastCenterPosition;
	private int mLastZoomLevel;
	private Timer mChangeDelayTimer = new Timer();
	
	private AirspaceLoader previous;
	private final PolygonOverlay mPolys;
	private final MapView mapView;
	
	private String host;
	private String[] classes;
	private final String[] known_classes;
	
	private int maxfloor;
	
	public AirspaceScrollListener(MapView mapView, PolygonOverlay polys, SharedPreferences prefs, Context context){
		this.mPolys = polys;
		
		this.host = prefs.getString("airspace_server_host", "http://airspace.kataplop.net:8888/api/v1");

		this.known_classes = context.getResources().getStringArray(R.array.airspace_classes);
		// If we could use MultiCheckbox from android >=3 ...
		HashSet<String> checked = new HashSet<String>();
		for (String clazz : known_classes){
			if (prefs.getBoolean("airspace_filter_class_" + clazz, true)){
				checked.add(clazz);
			}
		}
		this.classes = checked.toArray(new String[0]);

		// for multicheckbox... disabled for the moment.
		//this.classes = prefs.getStringSet("airspace_filter_class_pref", new HashSet<String>()).toArray(new String[0]);
		this.maxfloor = Integer.parseInt(prefs.getString("airspace_filter_max_floor_pref", "3000"));
		
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
		previous = new AirspaceLoader(mapView, mPolys, host, classes, maxfloor);
		previous.execute();
	}

	@Override
	public boolean onScroll(ScrollEvent scroll) {
		if (! mapView.getMapCenter().equals(mLastCenterPosition)){
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

	public void refresh(final SharedPreferences prefs, final String key) {
		if (key.equals("airspace_server_host")){
			this.host = prefs.getString("airspace_server_host", "http://airspace.kataplop.net:8888/api/v1");
		} else if (key.startsWith("airspace_filter_class_")){
			// If we could use MultiCheckbox from android >=3 ...
			HashSet<String> checked = new HashSet<String>();
			for (String clazz : this.known_classes){
				if (prefs.getBoolean("airspace_filter_class_" + clazz, true)){
					checked.add(clazz);
				}
			}
			this.classes = checked.toArray(new String[0]);
//			this.classes = prefs.getStringSet("airspace_filter_class_pref",
//					new HashSet<String>()).toArray(new String[0]);
		} else if (key.equals("airspace_filter_max_floor_pref")){
			this.maxfloor = Integer.parseInt(prefs.getString("airspace_filter_max_floor_pref", "3000"));
		}
	}
}
