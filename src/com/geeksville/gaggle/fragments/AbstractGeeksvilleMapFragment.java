package com.geeksville.gaggle.fragments;

import java.lang.Thread.UncaughtExceptionHandler;

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.overlay.MyLocationOverlay;

import com.flurry.android.FlurryAgent;
import com.geeksville.android.LifeCycleHandler;
import com.geeksville.android.LifeCyclePublisher;
import com.geeksville.android.LifeCyclePublisherImpl;
import com.geeksville.gaggle.GagglePrefs;
import com.geeksville.gaggle.R;
import com.geeksville.maps.CenteredMyLocationOverlay;
import com.geeksville.maps.GeeksvilleMapView;
import com.geeksville.util.GaggleUncaughtExceptionHandler;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;

public class AbstractGeeksvilleMapFragment extends Fragment implements LifeCyclePublisher {

	// private LinearLayout linearLayout;
	protected GeeksvilleMapView mapView;

	private MyLocationOverlay myLocationOverlay;

	private LifeCyclePublisherImpl lifePublish = new LifeCyclePublisherImpl();

	// There is also TopOSM features, but we don't bother to show that
//	private static final XYTileSource TopOSMRelief =
//			new XYTileSource("Topo Relief (USA)", ResourceProxy.string.unknown, 4, 15, 8, ".jpg",
//					"http://tile1.toposm.com/us/color-relief/",
//					"http://tile2.toposm.com/us/color-relief/",
//					"http://tile3.toposm.com/us/color-relief/");
//
//	private static final XYTileSource TopOSMContours =
//		new XYTileSource("Topo Contours (USA)", ResourceProxy.string.unknown, 12, 15, 8, ".png",
//				"http://tile1.toposm.com/us/contours/",
//				"http://tile2.toposm.com/us/contours/",
//				"http://tile3.toposm.com/us/contours/");
//
//	private static final XYTileSource OpenCycleMap =
//		new XYTileSource("www.opencyclemap.org", ResourceProxy.string.unknown, 1, 18, 8, ".png",
//				"http://a.tile.opencyclemap.org/cycle/",
//				"http://b.tile.opencyclemap.org/cycle/",
//				"http://c.tile.opencyclemap.org/cycle/"
//				);
//	private static final XYTileSource OpenHikingMap =
//		new XYTileSource("maps.refuges.info", ResourceProxy.string.unknown, 1, 18, 8, ".jpeg",
//				"http://maps.refuges.info/tiles/renderer.py/hiking/");
	
	private static OnlineTileSourceBase supportedRenderers[] = {
		TileSourceFactory.MAPQUESTAERIAL,
		TileSourceFactory.MAPNIK,
		TileSourceFactory.TOPO,
//			TopOSMContours,
//			TopOSMRelief
	};

	private String supportedRendererNames[];

	public AbstractGeeksvilleMapFragment() {
		initExceptionHandler();
		// FIXME - do this someplace better
//		TileSourceFactory.addTileSource(TopOSMContours);
//		TileSourceFactory.addTileSource(TopOSMRelief);
//		TileSourceFactory.addTileSource(OpenCycleMap);
//		TileSourceFactory.addTileSource(OpenHikingMap);
	}

	
	  public static GaggleUncaughtExceptionHandler initExceptionHandler()
	  {
	    GaggleUncaughtExceptionHandler exceptionHandler;
	    final UncaughtExceptionHandler ueh = Thread.getDefaultUncaughtExceptionHandler();

	    if ((ueh != null) && GaggleUncaughtExceptionHandler.class.isAssignableFrom(ueh.getClass())) {
	      exceptionHandler = (GaggleUncaughtExceptionHandler)ueh;
	    } else {
	      exceptionHandler = new GaggleUncaughtExceptionHandler(ueh);
	      Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
	    }

	    return exceptionHandler;
	  }
	
	
	  public void onCreate(Bundle savedInstanceState) {
		  super.onCreate(savedInstanceState);
		  supportedRendererNames = new String[] {
					getString(R.string.mapquestaerial_map),
					getString(R.string.mapnik_map),
					getString(R.string.toposm_map),
//					getString(R.string.openhikingmap),
//					getString(R.string.topo_europe),
//					getString(R.string.topo_us_contour),
//					getString(R.string.topo_us_relief)
			};
	  }
	
	/** Called when the activity is first created. */
	public View onCreateView(Bundle savedInstanceState, LayoutInflater inflater, ViewGroup container, int layoutId, int mapViewId) {
		// Our license key is different for the emulator, otherwise these files
		// should be identical
		View v = inflater.inflate(layoutId, container, false);

		mapView = (GeeksvilleMapView) v.findViewById(mapViewId);

		mapView.setBuiltInZoomControls(true);
		// Set default map view
		mapView.setTileSource(TileSourceFactory.MAPNIK);
		mapView.setMultiTouchControls(true);
		// Default to sat view
		// mapView.setSatellite(true);
		return v;
	}

	/**
	 * Collect app metrics on Flurry
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	public void onStart() {
		super.onStart();
		lifePublish.onStart();
	}

	/**
	 * Collect app metrics on Flurry
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	public void onStop() {
		super.onStop();
		lifePublish.onStop();
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		this.setHasOptionsMenu(true);
	}

	/**
	 * Create our options menu
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.map_optionmenu, menu);

		// Set action for map prefetch
		// menu.findItem(R.id.prefetch_map).setIntent(
		// PrefetchMapActivity.createIntent(this, mapView.getMapCenter(),
		// mapView
		// .getZoomLevel(), mapView.getRenderer().name()));

		// Dynamically populate the list of renderers we support (FIXME - only
		// list known good renderers)
		MenuItem mapoptions = menu.findItem(R.id.mapmode_menu);
		SubMenu children = mapoptions.getSubMenu();
		children.clear();

		MenuItem toCheck = null;
		for (int i = 0; i < supportedRenderers.length; i++) {
			final OnlineTileSourceBase info = supportedRenderers[i];
			String name = supportedRendererNames[i];

			MenuItem item = children.add(1, i, Menu.NONE, name);
			if (mapView.getTileProvider().getTileSource().name().equals(info.name()))
				toCheck = item;

			item.setOnMenuItemClickListener(new OnMenuItemClickListener() {

				@Override
				public boolean onMenuItemClick(MenuItem item) {
					mapView.setTileSource(info);
					item.setChecked(true);
					return true;
				}
			});
		}
		children.setGroupCheckable(1, true, true);
		toCheck.setChecked(true);
	}

	/**
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.myloc_menu) {
			zoomToLocation();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Override if you want a different my location view
	 * 
	 * @return
	 */
	protected MyLocationOverlay createLocationOverlay() {
		return new CenteredMyLocationOverlay(getActivity(), mapView);
	}

	/**
	 * If isLive is set, then add an overlay showing where user is
	 */
	protected void showCurrentPosition(boolean zoomToUser) {
		GagglePrefs prefs = new GagglePrefs(getActivity());
		if (prefs.isFlurryEnabled())
		  FlurryAgent.onEvent("View live position");

		myLocationOverlay = createLocationOverlay();

		if (zoomToUser) {
			// Once we find our position, center on it
			Runnable runnable = new Runnable() {

				@Override
				public void run() {
					zoomToLocation();
				}
			};
			myLocationOverlay.runOnFirstFix(runnable);
			
		}

		mapView.getOverlays().add(myLocationOverlay);
	}

	private void zoomToLocation() {
		// Center on where the user is
		MapController control = mapView.getController();

		GeoPoint loc;
		if (myLocationOverlay != null && (loc = myLocationOverlay.getMyLocation()) != null){
			control.animateTo(loc);
			// try to span (will depend on the layer capabilities) something reasonable...
			control.zoomToSpan(1, 1);
			myLocationOverlay.enableFollowLocation();
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		if (myLocationOverlay != null) {
			myLocationOverlay.disableMyLocation();
			myLocationOverlay.disableFollowLocation();
			// myLocationOverlay.disableCompass();
		}

		lifePublish.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();

		// Show our cur location
		if (myLocationOverlay != null) {
			myLocationOverlay.enableMyLocation();
			// myLocationOverlay.enableCompass();
		}

		lifePublish.onResume();
	}

	@Override
	public void addLifeCycleHandler(LifeCycleHandler h) {
		lifePublish.addLifeCycleHandler(h);
	}

	@Override
	public void removeLifeCycleHandler(LifeCycleHandler h) {
		lifePublish.removeLifeCycleHandler(h);
	}
}
