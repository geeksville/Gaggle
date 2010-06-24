/**
 * 
 */
package com.geeksville.gaggle;

import android.app.TabActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;

import com.flurry.android.FlurryAgent;
import com.geeksville.android.GeeksvilleExceptionHandler;
import com.geeksville.android.PostMortemReportExceptionHandler;
import com.geeksville.info.Units;
import com.geeksville.location.LocationLogDbAdapter;

/**
 * @author kevinh
 * 
 */
public class TopActivity extends TabActivity {

	/**
	 * Debugging tag
	 */
	private static final String TAG = "TopActivity";

	protected PostMortemReportExceptionHandler damageReport = new GeeksvilleExceptionHandler(
			this);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		damageReport.perhapsInstall();

		// FIXME, localize these strings
		TabHost tabHost = getTabHost();

		TabHost.TabSpec spec = tabHost.newTabSpec("FlightInst");
		// FIXME - need real art
		spec.setIndicator(null, getResources().getDrawable(R.drawable.icon));
		spec.setContent(new Intent(this, LoggingControl.class));
		tabHost.addTab(spec);

		spec = tabHost.newTabSpec("Logs");
		spec.setIndicator(null, getResources().getDrawable(android.R.drawable.ic_menu_slideshow));
		spec.setContent(new Intent(this, ListFlightsActivity.class));
		tabHost.addTab(spec);

		spec = tabHost.newTabSpec("Waypoints");
		spec.setIndicator(null, getResources().getDrawable(android.R.drawable.ic_menu_myplaces));
		spec.setContent(new Intent(this, ListWaypointsActivity.class));
		tabHost.addTab(spec);

		spec = tabHost.newTabSpec("LiveMap");
		spec.setIndicator(null, getResources().getDrawable(android.R.drawable.ic_menu_mapmode));
		spec.setContent(FlyMapActivity.createIntentLive(this));
		tabHost.addTab(spec);
	}

	/**
	 * Do we have any flights in our logbook?
	 * 
	 * @return
	 */
	private boolean isFlightsLogged() {
		LocationLogDbAdapter db = new LocationLogDbAdapter(this);
		Cursor flights = db.fetchAllFlights();

		int numflights = flights.getCount();

		flights.close();
		db.close();

		return numflights != 0;
	}

	/**
	 * Collect app metrics on Flurry
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		Log.d(TAG, "onStart() called");

		super.onStart();

		FlurryAgent.onStartSession(this, "XBPNNCR4T72PEBX17GKF");

		BetaSplashActivity.perhapsSplash(this);

		if (!isFlightsLogged()) {
			getTabHost().setCurrentTabByTag("Logs");
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (intent != null) {
			Bundle extras = intent.getExtras();
		}

		BetaSplashActivity.handleActivityResult(this, requestCode, resultCode);
	}

	/**
	 * Collect app metrics on Flurry
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		Log.d(TAG, "onStop() called");

		super.onStop();

		FlurryAgent.onEndSession(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// If we are leaving, we don't need the service no more
		((GaggleApplication) getApplication()).stopGPSClient();
	}

	/**
	 * @see android.app.ActivityGroup#onPause()
	 */
	@Override
	protected void onResume() {
		super.onResume();

		Units.instance.setFromPrefs(this); // This should take care of making
											// units changes work for all our
											// subviews, I
		// can probably remove the other calls (FIXME)

		// Figure out if the user wants to force the screen on (FIXME, only
		// force on while flying)
		TabHost tabHost = getTabHost();
		GagglePrefs prefs = new GagglePrefs(this);
		tabHost.setKeepScreenOn(prefs.isKeepScreenOn());
	}

	/**
	 * Create our options menu
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		getMenuInflater().inflate(R.menu.shared_options, menu);

		MenuItem menuItem = menu.findItem(R.id.preferences_menu);
		menuItem.setIntent(new Intent(this, MyPreferences.class));

		menuItem = menu.findItem(R.id.about_menu);
		menuItem.setIntent(new Intent(this, AboutActivity.class));

		return true;
	}
}
