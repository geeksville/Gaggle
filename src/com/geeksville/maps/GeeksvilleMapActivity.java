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

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.flurry.android.FlurryAgent;
import com.geeksville.android.LifeCycleHandler;
import com.geeksville.android.LifeCyclePublisher;
import com.geeksville.android.LifeCyclePublisherImpl;
import com.geeksville.gaggle.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class GeeksvilleMapActivity extends MapActivity implements LifeCyclePublisher {

	// private LinearLayout linearLayout;
	protected MapView mapView;

	private MyLocationOverlay myLocationOverlay;

	private LifeCyclePublisherImpl lifePublish = new LifeCyclePublisherImpl();

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
		case R.id.satellite_menu:
			mapView.setSatellite(true);
			return true;
		case R.id.street_menu:
			mapView.setSatellite(false);
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

		mapView = (MapView) findViewById(mapViewId);

		mapView.setBuiltInZoomControls(true);

		// Default to sat view
		mapView.setSatellite(true);
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
		MapController control = mapView.getController();

		GeoPoint loc;
		if (myLocationOverlay != null && (loc = myLocationOverlay.getMyLocation()) != null)
			control.animateTo(loc);
	}

	/**
	 * @see com.google.android.maps.MapActivity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();

		if (myLocationOverlay != null) {
			myLocationOverlay.disableMyLocation();
			myLocationOverlay.disableCompass();
		}

		lifePublish.onPause();
	}

	/**
	 * @see com.google.android.maps.MapActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();

		// Show our cur location
		if (myLocationOverlay != null) {
			myLocationOverlay.enableMyLocation();
			myLocationOverlay.enableCompass();
		}

		lifePublish.onResume();
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
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
