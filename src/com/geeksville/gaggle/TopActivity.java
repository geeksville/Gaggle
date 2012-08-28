/**
 * 
 */
package com.geeksville.gaggle;

import java.io.File;
import java.util.HashMap;

import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;

import com.flurry.android.FlurryAgent;
import com.geeksville.android.PreferenceUtil;
import com.geeksville.billing.Donate;
import com.geeksville.gaggle.fragments.FlyMapFragment;
import com.geeksville.gaggle.fragments.ListFlightsFragment;
import com.geeksville.gaggle.fragments.ListWaypointsFragment;
import com.geeksville.gaggle.fragments.LoggingControlFragment;
import com.geeksville.info.Units;
import com.geeksville.location.LocationList;
import com.geeksville.location.LocationLogDbAdapter;
import com.geeksville.view.AsyncProgressDialog;

/**
 * @author kevinh
 * 
 */
public class TopActivity extends Activity implements
		TabHost.OnTabChangeListener,
		ListFlightsFragment.OnFlightSelectedListener {

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
				.newTabSpec(FLIGHT_CLTR_TAB).setIndicator("Tab 1"),
				(tabInfo = new TabInfo(FLIGHT_CLTR_TAB, LoggingControlFragment.class, args)));
		this.mapTabInfo.put(tabInfo.tag, tabInfo);
		
		TopActivity.addTab(this, this.mTabHost, this.mTabHost
				.newTabSpec(FLIGHT_LOGS_TAB).setIndicator("Tab 2"),
				(tabInfo = new TabInfo(FLIGHT_LOGS_TAB, ListFlightsFragment.class, args)));
		this.mapTabInfo.put(tabInfo.tag, tabInfo);
		
		TopActivity.addTab(this, this.mTabHost, this.mTabHost
				.newTabSpec(WAYPOINTS_TAB).setIndicator("Tab 3"),
				(tabInfo = new TabInfo(WAYPOINTS_TAB, ListWaypointsFragment.class, args)));
		this.mapTabInfo.put(tabInfo.tag, tabInfo);
		
		TopActivity.addTab(this, this.mTabHost, this.mTabHost
				.newTabSpec(FLYMAP_TAB).setIndicator("Tab 4"),
				(tabInfo = new TabInfo(FLYMAP_TAB, FlyMapFragment.class, args)));
		this.mapTabInfo.put(tabInfo.tag, tabInfo);
				mTabHost.setOnTabChangedListener(this);
	}

	@Override
	public void onTabChanged(String tag) {
		TabInfo newTab = this.mapTabInfo.get(tag);

		if (mLastTab != newTab) {
			FragmentTransaction ft = this.getFragmentManager()
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
			this.getFragmentManager().executePendingTransactions();
		}
	}

	@Override
	public void onFlightSelected(LocationList locs) {
		Log.d(TAG, "received onFlightSelect event, prepare new activity");

		Bundle locbundle = new Bundle();
		locs.writeTo(locbundle);
		Intent i = new Intent(this, FlyMapActivity.class);
		i.putExtra(FlyMapFragment.EXTRA_TRACKLOG, locbundle);
		startActivity(i);
	}

	private static void addTab(TopActivity activity, TabHost tabHost,
			TabHost.TabSpec tabSpec, TabInfo tabInfo) {
		// Attach a Tab view factory to the spec
		tabSpec.setContent(activity.new TabFactory(activity));
		String tag = tabSpec.getTag();

		// Check to see if we already have a fragment for this tab, probably
		// from a previously saved state. If so, deactivate it, because our
		// initial state is that a tab isn't shown.
		tabInfo.fragment = activity.getFragmentManager()
				.findFragmentByTag(tag);
		if (tabInfo.fragment != null && !tabInfo.fragment.isDetached()) {
			FragmentTransaction ft = activity.getFragmentManager()
					.beginTransaction();
			ft.detach(tabInfo.fragment);
			ft.commit();
			activity.getFragmentManager().executePendingTransactions();
		}

		tabHost.addTab(tabSpec);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "creating topactivity!");

		// setThemeFromPrefs();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.maintabs);
		initialiseTabHost(savedInstanceState);
//		if (savedInstanceState != null) {
//			mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
//		}
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

    Donate d = new Donate(this);
    d.perhapsSplash();

    updateFromOld();

    // always start in first tab
    mTabHost.setCurrentTab(0);
    onTabChanged(FLIGHT_CLTR_TAB);
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
          true);

      if (newLight != isLightTheme) {
        // Restart our app with the new theme
        finish();
        startActivity(new Intent(this, TopActivity.class));
      }
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

    // No need to show this for now...
    menu.findItem(R.id.donate_menu).setVisible(
        !Donate.isDonated(this) && Donate.canPromptToUpdate(this));

    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.preferences_menu:
      startActivityForResult(new Intent(this, MyPreferences.class), SHOW_PREFS);
      return true;

    case R.id.donate_menu:
      Donate d = new Donate(this);
      d.splash();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
