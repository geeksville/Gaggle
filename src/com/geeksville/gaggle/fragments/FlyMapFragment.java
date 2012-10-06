package com.geeksville.gaggle.fragments;

import java.io.IOException;
import java.io.InputStream;
import java.util.Observable;
import java.util.Observer;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.pedro.balises.ffvl.FfvlProvider;

import com.flurry.android.FlurryAgent;
import com.geeksville.airspace.AirspaceScrollListener;
import com.geeksville.android.AndroidUtil;
import com.geeksville.gaggle.AltitudeView;
import com.geeksville.gaggle.GagglePrefs;
import com.geeksville.gaggle.R;
import com.geeksville.info.Units;
import com.geeksville.location.IGCReader;
import com.geeksville.location.LocationList;
import com.geeksville.location.Waypoint;
import com.geeksville.maps.CenteredMyLocationOverlay;
import com.geeksville.maps.PolygonOverlay;
import com.geeksville.maps.TracklogOverlay;
import com.geeksville.maps.WaypointOverlay;
import com.geeksville.weather.overlay.WeatherStationsOverlay;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FlyMapFragment  extends AbstractGeeksvilleMapFragment 
	implements Observer, OnSharedPreferenceChangeListener {

	private static final String TAG = "FlyMapFragment";
	/**
	 * Extra data we look for in our Intent. If specified it will be a Bundle
	 * generated by LocationList
	 */
	public static final String EXTRA_TRACKLOG = "tracklog";

	/**
	 * Extra intend data, a boolean, true == show the user's position
	 */
	private static final String EXTRA_ISLIVE = "live";

	/**
	 * Are we showing the current user position?
	 */
	private boolean isLive = false;

	private WaypointOverlay wptOver;
	private PolygonOverlay polyOver;

	private WeatherStationsOverlay weather_overlay;

	private AirspaceScrollListener airspace_scroll_lst;
	private AltitudeView altitudeView;

	// FIXME - skanky, find a better way to pass in ptrs when we ain't
	// crossing process boundaries
	public static LocationList liveList;


	@Override
	public void onActivityCreated(Bundle savedInstance){
		super.onActivityCreated(savedInstance);
		this.setHasOptionsMenu(true);
	}

	/**
	 * Collect app metrics on Flurry
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	public void onStart() {
		super.onStart();
		GagglePrefs prefs = new GagglePrefs(getActivity());
	    if (prefs.isFlurryEnabled())
		  FlurryAgent.onStartSession(getActivity(), "XBPNNCR4T72PEBX17GKF");
	}

	/**
	 * Collect app metrics on Flurry
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	public void onStop() {
		super.onStop();

		GagglePrefs prefs = new GagglePrefs(getActivity());
		if (prefs.isFlurryEnabled())
			FlurryAgent.onEndSession(getActivity());

		// Save current map position
		prefs.setMapCenterZoom(mapView.getMapCenter().getLatitudeE6(),
				mapView.getMapCenter().getLongitudeE6(),
				mapView.getZoomLevel());
	}

	@Override
	public void onCreate(Bundle args){
		super.onCreate(args);
	}

	/*
	 * 
	 */
	public void setCenterOnWaypoint(Waypoint wpt){
		final GeoPoint center = new GeoPoint(wpt.latitude, wpt.longitude);
		mapView.getController().setCenter(center);
//		mapView.getController().setZoom(zoom);
	}

	/** Called when the activity is first created. */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Intent intent = getActivity().getIntent();

		Bundle extras = intent.getExtras();

		if (extras != null) {
			// Do we show the current user position?
			isLive = extras.getBoolean(FlyMapFragment.EXTRA_ISLIVE, isLive);
		}

		int layoutId = isLive ? R.layout.flymap_live_fragment : R.layout.flymap_delayed_fragment;
		int mapviewId = isLive ? R.id.mapview_live : R.id.mapview_delayed;

		View v = super.onCreateView(savedInstanceState,inflater, container, layoutId, mapviewId);

		altitudeView = (AltitudeView) v.findViewById(R.id.altitude_view);

		addWaypoints();
		addPolyoverlay();
		perhapsAddFromUri();
		perhapsAddExtraTracklog();

		// get the last center/zoom
		// This may trigger a small flickering if 
		// current user location is available (GeeksvilleMapA will
		// zoom on this location immediately)
		GagglePrefs prefs = new GagglePrefs(getActivity());
		final int latE6 = prefs.getMapCenterZoom_Lat();
		final int lonE6 = prefs.getMapCenterZoom_Lon();
		final int zoom = prefs.getMapCenterZoom_Zoom();
		if (latE6 != -1 && lonE6 != -1 && zoom != -1) {
			final GeoPoint center = new GeoPoint(latE6,  lonE6);
			mapView.getController().setCenter(center);
			mapView.getController().setZoom(zoom);
		}
		if (isLive){
			showCurrentPosition(true);
		}
		if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("airspace_enable", false)){
			enableAirspaceManagement();
		}
		if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("weather_stations_enable", false)){
			Log.d("FMA", "weather enabled on start");
			enableWeatherStations();
		}
		return v;
	}

	/**
	 * See if the user wants us to open an IGC file
	 */
	private void perhapsAddFromUri() {
		Uri uri = getActivity().getIntent().getData();
		String action = getActivity().getIntent().getAction();

		if (uri != null && action != null && action.equals(Intent.ACTION_VIEW)) {
			// See if we can read the file
			try {
				GagglePrefs prefs = new GagglePrefs(getActivity());
			    if (prefs.isFlurryEnabled())
				  FlurryAgent.onEvent("IGC view start");

				InputStream s = AndroidUtil.getFromURI(getActivity(), uri);

				IGCReader iread = new IGCReader("gps", s);
				LocationList loclist = iread.toLocationList();
				iread.close();
				
			    if (prefs.isFlurryEnabled())
				  FlurryAgent.onEvent("IGC view success");

				// Show the points
				altitudeView.setLocs(loclist);
				mapView.getOverlays().add(createTracklogOverlay(loclist));
			} catch (IOException ex) {
				GagglePrefs prefs = new GagglePrefs(getActivity());
				if (prefs.isFlurryEnabled())
				  FlurryAgent.onEvent("IGC view fail");

				// FIXME - move this alert goo into a standard localized utility
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle(R.string.unable_to_open_igc_file);
				builder.setMessage(ex.toString());
				builder.setPositiveButton(R.string.okay, null);

				AlertDialog alert = builder.create();
				alert.show();
			}
		}
	}

	private void addWaypoints() {
		wptOver = new WaypointOverlay(getActivity(), mapView);
		mapView.getOverlays().add(wptOver);
	}

	private void addPolyoverlay() {
		polyOver = new PolygonOverlay(getActivity());
		mapView.getOverlays().add(polyOver);
	}

	/**
	 * If a tracklog was added to our intent, then show it
	 */
	private void perhapsAddExtraTracklog() {
		Bundle args = getArguments();

		// Parse our params
		if (args != null && args.getBoolean(EXTRA_TRACKLOG)) {
			// Any stored tracklogs? if yes, then show them
			LocationList locs = new LocationList(args);

			altitudeView.setLocs(locs);
			mapView.getOverlays().add(createTracklogOverlay(locs));
		}
	}
//	
//	private void handleAirspaceTrigger(){
//		if (mapView.getOnScrollChangeListener() == null){
//			mapView.setOnScrollChangeListener(new AirspaceScrollListener(polyOver));
//		} else {
//			mapView.setOnScrollChangeListener(null);
//		}
//	}

	private void enableAirspaceManagement(){
//		if (mapView.getOnScrollChangeListener() == null){
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			airspace_scroll_lst = new AirspaceScrollListener(mapView, polyOver, prefs, getActivity());
			mapView.setMapListener(airspace_scroll_lst);
			airspace_scroll_lst.update();
//			mapView.setOnScrollChangeListener(airspace_scroll_lst);
//		}
		// else { error } => should not get enable if already enabled
	}
	private void disableAirspaceManagement(){
//		if (mapView.getOnScrollChangeListener() != null){
			//mapView.setOnScrollChangeListener(null);
		mapView.setMapListener(null);
			airspace_scroll_lst = null;
//		}
		// else { error } => should not get disable if already disabled
	}

	private void enableWeatherStations(){
		Log.d("FMA", "Enabling weather");
		weather_overlay = new WeatherStationsOverlay(getActivity(), mapView);

		mapView.getOverlays().add(weather_overlay);
	}

	private void disableWeatherStations(){
		Log.d("FMA", "Disabling weather");
		mapView.getOverlays().remove(weather_overlay);
	}

	/**
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//
//		case R.id.airspacemode_menu:
////			handleAirspaceTrigger();
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}
	
	@Override
	public void onPause() {
		super.onPause();

		wptOver.onPause();

		// Remove any live tracklog (in case the next instance doesn't want it -
		// YUCK)
		if (liveTracklogOverlay != null) {
			liveList.deleteObserver(this);

			mapView.getOverlays().remove(liveTracklogOverlay);
			liveTracklogOverlay = null;
		}
	}

	/**
	 * FIXME, move all the live map stuff into a subclass?
	 */
	private TracklogOverlay liveTracklogOverlay;

	@Override
	public void onResume() {
		super.onResume();

		Units.instance.setFromPrefs(getActivity());
		wptOver.onResume();

		// Show our latest live tracklog
		if (liveList != null && isLive) {
			LocationList locs = liveList; // Super skanky, find a better way
			// to pass in ptrs FIXME.

			liveList.addObserver(this);

			altitudeView.setLocs(locs);
			liveTracklogOverlay = new TracklogOverlay(getActivity(), locs);
			mapView.getOverlays().add(liveTracklogOverlay);
		} else {
			perhapsAddExtraTracklog();
		}

//		new AirspaceLoader().execute();
	}

	/**
	 * Create an overlay for a canned tracklog
	 * 
	 * @param locBundle
	 */
	private TracklogOverlay createTracklogOverlay(final LocationList locs) {
		Log.d(TAG, "create Track log");
		// Show a tracklog
		TracklogOverlay tlog = new TracklogOverlay(getActivity(), locs);

		// Center on the tracklog (we do this in a callback, because OSM only
		// works after the view has been layed out
		if (locs.numPoints() > 0) {
			mapView.setPostLayout(new Runnable() {

				@Override
				public void run() {
					MapController control = mapView.getController();

					control.setCenter(locs.getGeoPoint(0));

					// it is about 80 feet to one second of a degree. So if we
					// want to
					// limit the max default zoom to about 500 feet
					double maxZoomFeet = 2500;
					double feetPerSecond = 80;
					double minDegrees = (maxZoomFeet / feetPerSecond) / 60 / 60;
					int minDegreesE6 = (int) (minDegrees * 1e6);

					// FIXME - the following is busted on OSM, it must be called
					// later -
					// after the view has been sized and fully created
					control.zoomToSpan(Math.max(locs.latitudeSpanE6(), minDegreesE6), Math.max(locs
							.longitudeSpanE6(), minDegreesE6));
					Log.d(TAG, "Tracklog should be displayed");
					// control.setZoom(11); // hack till fixed for OSM
				}
			});
		}

		return tlog;
	}

	/**
	 * Use our spiffy heading display
	 * 
	 * @see com.geeksville.maps.GeeksvilleMapActivity#createLocationOverlay()
	 */
	@Override
	protected MyLocationOverlay createLocationOverlay() {
		return new CenteredMyLocationOverlay(getActivity(), mapView);
	}

	/**
	 * Called when our tracklog gets updated
	 * 
	 * @param observable
	 * @param data
	 */
	@Override
	public void update(Observable observable, Object data) {
		mapView.postInvalidateDelayed(1000);

	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equals("airspace_enable")){
			if (sharedPreferences.getBoolean(key, false)){
				enableAirspaceManagement();
			} else {
				disableAirspaceManagement();
			}
		} else if (key.equals("weather_stations_enable")) {
			if (sharedPreferences.getBoolean(key, false)){
				enableWeatherStations();
			} else {
				disableWeatherStations();
			}
		} else {
			if (airspace_scroll_lst != null)
				airspace_scroll_lst.refresh(sharedPreferences, key);
		}
	}
}
