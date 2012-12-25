/**
 * 
 */
package com.geeksville.gaggle;

import java.io.File;
import java.util.HashMap;

import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;

import com.flurry.android.FlurryAgent;
import com.geeksville.android.PreferenceUtil;
import com.geeksville.gaggle.fragments.FlyMapFragment;
import com.geeksville.gaggle.fragments.ListFlightsFragment;
import com.geeksville.gaggle.fragments.ListWaypointsFragment;
import com.geeksville.gaggle.fragments.LoggingControlFragment;
import com.geeksville.info.Units;
import com.geeksville.location.LocationList;
import com.geeksville.location.LocationLogDbAdapter;
import com.geeksville.location.Waypoint;
import com.geeksville.view.AsyncProgressDialog;

/**
 * @author kevinh
 * 
 */
public class TopActivity extends FragmentActivity implements
		TabHost.OnTabChangeListener,
		ListFlightsFragment.OnFlightSelectedListener,
		ListWaypointsFragment.OnWaypointActionListener {

  /**
   * Debugging tag
   */
  private static final String TAG = "TopActivity";

  private static final String FLIGHT_CLTR_TAB = "FlightCtrl";
  private static final String FLIGHT_LOGS_TAB = "Logs";
  private static final String WAYPOINTS_TAB = "Waypoints";
  private static final String FLYMAP_TAB = "FLyMap";

  // An activity request code
  private static final int SHOW_PREFS = 1;

  private boolean isLightTheme;

  
	private TabHost mTabHost;
	private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, TabInfo>();
	private TabInfo mLastTab = null;
	private int mTabIndexForPause;

	private class TabInfo {
		private String tag;
		private Class clss;
		private Bundle args;
		private Fragment fragment;

		TabInfo(String tag, Class clazz, Bundle args) {
			this.tag = tag;
			this.clss = clazz;
			this.args = args;
		}
	}

	class TabFactory implements TabContentFactory {

		private final Context mContext;

		/**
		 * @param context
		 */
		public TabFactory(Context context) {
			mContext = context;
		}

		/**
		 * (non-Javadoc)
		 * 
		 * @see android.widget.TabHost.TabContentFactory#createTabContent(java.lang.String)
		 */
		public View createTabContent(String tag) {
			View v = new View(mContext);
			v.setMinimumWidth(0);
			v.setMinimumHeight(0);
			return v;
		}
	}

	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("tab", mTabHost.getCurrentTabTag());
		super.onSaveInstanceState(outState);
	}

	private void initialiseTabHost(Bundle args) {
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();
		TabInfo tabInfo = null;

		TopActivity.addTab(this, this.mTabHost, this.mTabHost
				.newTabSpec(FLIGHT_CLTR_TAB).setIndicator(null, getResources().getDrawable(R.drawable.icon)),
				(tabInfo = new TabInfo(FLIGHT_CLTR_TAB, LoggingControlFragment.class, args)));
		this.mapTabInfo.put(tabInfo.tag, tabInfo);
		
		TopActivity.addTab(this, this.mTabHost, this.mTabHost
				.newTabSpec(FLIGHT_LOGS_TAB).setIndicator(null,
						getResources().getDrawable(android.R.drawable.ic_menu_slideshow)),
						(tabInfo = new TabInfo(FLIGHT_LOGS_TAB, ListFlightsFragment.class, args)));
		this.mapTabInfo.put(tabInfo.tag, tabInfo);

		TopActivity.addTab(this, this.mTabHost, this.mTabHost
				.newTabSpec(WAYPOINTS_TAB).setIndicator(null,
						getResources().getDrawable(android.R.drawable.ic_menu_myplaces)),
						(tabInfo = new TabInfo(WAYPOINTS_TAB, ListWaypointsFragment.class, args)));
		this.mapTabInfo.put(tabInfo.tag, tabInfo);
		Bundle flyMapArgs;
		if(args!=null)
			// drawing a copy is safer than using than modifying 'args' directly. 
			flyMapArgs = new Bundle(args);
		else
			flyMapArgs = new Bundle();
		// tell FlyMapFragment to show the current position:
		flyMapArgs.putBoolean(FlyMapFragment.EXTRA_ISLIVE, true);
		TopActivity.addTab(this, this.mTabHost, this.mTabHost
				.newTabSpec(FLYMAP_TAB).setIndicator(null,
						getResources().getDrawable(android.R.drawable.ic_menu_mapmode)),
						(tabInfo = new TabInfo(FLYMAP_TAB, FlyMapFragment.class, flyMapArgs)));
		this.mapTabInfo.put(tabInfo.tag, tabInfo);
				mTabHost.setOnTabChangedListener(this);
	}

	@Override
	public void onTabChanged(String tag) {
		TabInfo newTab = this.mapTabInfo.get(tag);

		if (mLastTab != newTab) {
			FragmentTransaction ft = this.getSupportFragmentManager()
					.beginTransaction();
			if (mLastTab != null) {
				if (mLastTab.fragment != null) {
					ft.hide(mLastTab.fragment);
				}
			}

			if (newTab != null) {
				Log.d(TAG, "new tab: " + newTab.tag);
				if (newTab.fragment == null) {
					newTab.fragment = Fragment.instantiate(this,
							newTab.clss.getName(), newTab.args);
					ft.add(R.id.realtabcontent, newTab.fragment, newTab.tag);
				} else {
					ft.show(newTab.fragment);
				}
			}
			mLastTab = newTab;
			ft.commit();
			this.getSupportFragmentManager().executePendingTransactions();
		}
	}

	@Override
	public void onFlightSelected(LocationList locs) {
		Log.d(TAG, "received onFlightSelect event");

		Bundle locbundle = new Bundle();
		locs.writeTo(locbundle);
		Intent i = new Intent(this, FlyMapActivity.class);
		i.putExtra(FlyMapFragment.EXTRA_TRACKLOG, locbundle);
		startActivity(i);
	}

	@Override
	public void onWaypointSelected(Waypoint wpt){
		Log.d(TAG, "received onWaypointSelect event");
		onTabChanged(FLYMAP_TAB);
		TabInfo newTab = this.mapTabInfo.get(FLYMAP_TAB);

		FlyMapFragment fmfrag = (FlyMapFragment) newTab.fragment;

		mTabHost.setCurrentTabByTag(FLYMAP_TAB);
		fmfrag.setCenterOnWaypoint(wpt);
	}

	private static void addTab(TopActivity activity, TabHost tabHost,
			TabHost.TabSpec tabSpec, TabInfo tabInfo) {
		// Attach a Tab view factory to the spec
		tabSpec.setContent(activity.new TabFactory(activity));
		String tag = tabSpec.getTag();

		// Check to see if we already have a fragment for this tab, probably
		// from a previously saved state. If so, deactivate it, because our
		// initial state is that a tab isn't shown.
		tabInfo.fragment = activity.getSupportFragmentManager()
				.findFragmentByTag(tag);
		if (tabInfo.fragment != null && !tabInfo.fragment.isDetached()) {
			FragmentTransaction ft = activity.getSupportFragmentManager()
					.beginTransaction();
			ft.detach(tabInfo.fragment);
			ft.commit();
			activity.getSupportFragmentManager().executePendingTransactions();
		}

		tabHost.addTab(tabSpec);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "creating topactivity!");

		setThemeFromPrefs();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.maintabs);
		initialiseTabHost(savedInstanceState);
		int oldTab = mTabHost.getCurrentTab();
		if(savedInstanceState == null){
			// try to get it from the intent, because in case of a theme switch the saveInstanceStae might not have been retained throught the normal path:
			savedInstanceState = getIntent().getBundleExtra("savedInstanceState");
		}
		if (savedInstanceState != null) {
			mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
			// and overrule the potential 0-value of the current tab, because o the re-init due to theme-switch (see also OnResume): 
			mTabIndexForPause = mTabHost.getCurrentTab();
		}
		// if tab has not been changed, then initiliaze it anyway:
		if(oldTab == mTabHost.getCurrentTab()){
			onTabChanged(mTabHost.getCurrentTabTag());
		}
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
    
    GagglePrefs prefs = new GagglePrefs(this);
	if (prefs.isFlurryEnabled())
      FlurryAgent.onStartSession(this, "XBPNNCR4T72PEBX17GKF");

    BetaSplashActivity.perhapsSplash(this);

    updateFromOld();

//    // always start in first tab
//    mTabHost.setCurrentTab(0);
//    onTabChanged(FLIGHT_CLTR_TAB);
  }

  private static final int MIN_GOOD_VERSION = 1;

  /**
   * Check to see if the app has all the updates needed to the filesystem (using
   * a build #)
   */
  private void updateFromOld() {
    final SharedPreferences prefs = getPreferences(MODE_PRIVATE);
    int oldVer = prefs.getInt("prefsVer", 0);

    if (oldVer < MIN_GOOD_VERSION) {
      AsyncProgressDialog progress = new AsyncProgressDialog(this,
          getString(R.string.updating_from_a_prior_release),
          getString(R.string.please_wait)) {

        @Override
        protected void doInBackground() {
          wipeBadOSMTiles();
          prefs.edit().putInt("prefsVer", MIN_GOOD_VERSION).commit();
        }

      };
      progress.execute();
    }
  }

  /**
   * There was a bad OSM load that used suffixes that confused the image library
   */
  private void wipeBadOSMTiles() {
    File dir = OpenStreetMapTileProviderConstants.TILE_PATH_BASE;

    deleteAllImages(dir);
  }

  /**
   * Recursively delete all image files in a directory
   * 
   * @param dir
   */
  private void deleteAllImages(File dir) {

    if (dir.isDirectory()) {
      File[] children = dir.listFiles();

      for (File f : children)
        deleteAllImages(f);
    } else {
      String filename = dir.getName().toLowerCase();

      if (filename.endsWith(".jpg") || filename.endsWith(".png")) {
        Log.d(TAG, "Deleting " + dir);
        dir.delete();
      }
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    if (intent != null) {
      Bundle extras = intent.getExtras();
    }

    if (requestCode == SHOW_PREFS) {
      boolean newLight = PreferenceUtil.getBoolean(this, "use_light_theme",
          false);

      if (newLight != isLightTheme) {
        // Restart our app with the new theme
        finish();
        intent = createIntentWithSavedStateInfo();
        startActivity(intent);
      }
    }
    BetaSplashActivity.handleActivityResult(this, requestCode, resultCode);
  }

private Intent createIntentWithSavedStateInfo() {
	Intent intent;
	intent = new Intent(this, TopActivity.class);
	Bundle bundle = new Bundle();
	onSaveInstanceState(bundle);
	intent.putExtra("savedInstanceState",bundle);
	return intent;
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
    
    GagglePrefs prefs = new GagglePrefs(this);
	if (prefs.isFlurryEnabled())
      FlurryAgent.onEndSession(this);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    // If we are leaving, we don't need the service no more
    ((GaggleApplication) getApplication()).stopGPSClient();
  }

  private void setThemeFromPrefs() {
    // Set light theme if the user wants a mostly white GUI
    isLightTheme = PreferenceUtil.getBoolean(this, "use_light_theme", false);
    int t = isLightTheme ? android.R.style.Theme_Light_NoTitleBar
        : android.R.style.Theme_NoTitleBar;
    setTheme(t);
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
    GagglePrefs prefs = new GagglePrefs(this);
    mTabHost.setKeepScreenOn(prefs.isKeepScreenOn());
    mTabHost.setCurrentTab(mTabIndexForPause);
  }

  @Override
  protected void onPause(){
	  mTabIndexForPause = mTabHost.getCurrentTab();
	  super.onPause();
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

    MenuItem menuItem = menu.findItem(R.id.about_menu);
    menuItem.setIntent(new Intent(this, AboutActivity.class));
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();
	if (itemId == R.id.preferences_menu) {
		startActivityForResult(new Intent(this, MyPreferences.class), SHOW_PREFS);
		return true;
	} 
    return super.onOptionsItemSelected(item);
  }
}
