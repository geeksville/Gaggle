/*******************************************************************************
 * Gaggle is Copyright 2010 by Geeksville Industries LLC, a California limited liability corporation. 
 * 
 * Gaggle is distributed under a dual license.  We've chosen this approach because within Gaggle we've used a number
 * of components that Geeksville Industries LLC might reuse for commercial products.  Gaggle can be distributed under
 * either of the two licenses listed below.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details. 
 * 
 * Commercial Distribution License
 * If you would like to distribute Gaggle (or portions thereof) under a license other than 
 * the "GNU General Public License, version 2", contact Geeksville Industries.  Geeksville Industries reserves
 * the right to release Gaggle source code under a commercial license of its choice.
 * 
 * GNU Public License, version 2
 * All other distribution of Gaggle must conform to the terms of the GNU Public License, version 2.  The full
 * text of this license is included in the Gaggle source, see assets/manual/gpl-2.0.txt.
 ******************************************************************************/
package com.geeksville.maps;

import org.andnav.osm.ResourceProxy;
import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapViewController;
import org.andnav.osm.views.overlay.MyLocationOverlay;
import org.andnav.osm.views.util.IOpenStreetMapRendererInfo;
import org.andnav.osm.views.util.OpenStreetMapRendererFactory;
import org.andnav.osm.views.util.XYRenderer;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.MenuItem.OnMenuItemClickListener;

import com.flurry.android.FlurryAgent;
import com.geeksville.android.LifeCycleHandler;
import com.geeksville.android.LifeCyclePublisher;
import com.geeksville.android.LifeCyclePublisherImpl;
import com.geeksville.gaggle.R;

/**
 * 
 * @author kevinh FIXME, add the following sources: http://toposm.com/usw/ Great
 *         topos here: http://openpistemap.org/?lat=41&lon=-100&zoom=12
 * 
 */
public class GeeksvilleMapActivity extends Activity implements LifeCyclePublisher {

	// private LinearLayout linearLayout;
	protected GeeksvilleMapView mapView;

	private MyLocationOverlay myLocationOverlay;

	private LifeCyclePublisherImpl lifePublish = new LifeCyclePublisherImpl();

	// There is also TopOSM features, but we don't bother to show that
	private static final IOpenStreetMapRendererInfo TopOSMRelief =
			new XYRenderer("Topo Relief (USA)", ResourceProxy.string.unknown, 4, 15, 8, ".jpg",
					"http://tile1.toposm.com/us/color-relief/",
					"http://tile2.toposm.com/us/color-relief/",
					"http://tile3.toposm.com/us/color-relief/");

	private static final IOpenStreetMapRendererInfo TopOSMContours =
			new XYRenderer("Topo Contours (USA)", ResourceProxy.string.unknown, 12, 15, 8, ".png",
					"http://tile1.toposm.com/us/contours/",
					"http://tile2.toposm.com/us/contours/",
					"http://tile3.toposm.com/us/contours/");

	private static IOpenStreetMapRendererInfo supportedRenderers[] = {
			// OpenStreetMapRendererFactory.MAPNIK,
			OpenStreetMapRendererFactory.OSMARENDER,
			OpenStreetMapRendererFactory.TOPO,
			TopOSMContours,
			TopOSMRelief
	};

	private static String supportedRendererNames[] = {
			"Street Map",
			"Topo (Europe)",
			"Topo (US contour)",
			"Topo (US relief)"
	};

	public GeeksvilleMapActivity() {
		// FIXME - do this someplace better
		OpenStreetMapRendererFactory.addRenderer(TopOSMContours);
		OpenStreetMapRendererFactory.addRenderer(TopOSMRelief);
	}

	/**
	 * Collect app metrics on Flurry
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		lifePublish.onStart();
	}

	/**
	 * Collect app metrics on Flurry
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();

		lifePublish.onStop();
	}

	/**
	 * Create our options menu
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		getMenuInflater().inflate(R.menu.map_optionmenu, menu);

		// Set action for map prefetch
		menu.findItem(R.id.prefetch_map).setIntent(
				PrefetchMapActivity.createIntent(this, mapView.getMapCenter(), mapView
						.getZoomLevel(), mapView.getRenderer().name()));

		// Dynamically populate the list of renderers we support (FIXME - only
		// list known good renderers)
		MenuItem mapoptions = menu.findItem(R.id.mapmode_menu);
		SubMenu children = mapoptions.getSubMenu();
		children.clear();

		MenuItem toCheck = null;
		for (int i = 0; i < supportedRenderers.length; i++) {
			final IOpenStreetMapRendererInfo info = supportedRenderers[i];
			String name = supportedRendererNames[i];

			MenuItem item = children.add(1, i, Menu.NONE, name);

			if (mapView.getRenderer().name().equals(info.name()))
				toCheck = item;

			item.setOnMenuItemClickListener(new OnMenuItemClickListener() {

				@Override
				public boolean onMenuItemClick(MenuItem item) {
					mapView.setRenderer(info);
					item.setChecked(true);
					return true;
				}
			});
		}
		children.setGroupCheckable(1, true, true);
		toCheck.setChecked(true);

		return true;
	}

	/**
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.myloc_menu:
			zoomToLocation();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState, int layoutId, int mapViewId) {
		super.onCreate(savedInstanceState);

		// Our license key is different for the emulator, otherwise these files
		// should be identical
		setContentView(layoutId);

		mapView = (GeeksvilleMapView) findViewById(mapViewId);

		mapView.setBuiltInZoomControls(true);

		// Default to sat view
		// mapView.setSatellite(true);
	}

	/**
	 * Override if you want a different my location view
	 * 
	 * @return
	 */
	protected MyLocationOverlay createLocationOverlay() {
		return new CenteredMyLocationOverlay(this, mapView);
	}

	/**
	 * If isLive is set, then add an overlay showing where user is
	 */
	protected void showCurrentPosition(boolean zoomToUser) {
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
		OpenStreetMapViewController control = mapView.getController();

		GeoPoint loc;
		if (myLocationOverlay != null && (loc = myLocationOverlay.getMyLocation()) != null)
			control.animateTo(loc);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (myLocationOverlay != null) {
			myLocationOverlay.disableMyLocation();
			// myLocationOverlay.disableCompass();
		}

		lifePublish.onPause();
	}

	@Override
	protected void onResume() {
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
