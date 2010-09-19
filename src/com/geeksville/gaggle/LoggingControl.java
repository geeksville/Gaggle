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
package com.geeksville.gaggle;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.flurry.android.FlurryAgent;
import com.geeksville.android.AndroidUtil;
import com.geeksville.android.ChangeHandler;
import com.geeksville.android.LifeCycleHandler;
import com.geeksville.android.LifeCyclePublisher;
import com.geeksville.android.LifeCyclePublisherImpl;
import com.geeksville.info.InfoListView;
import com.geeksville.info.SelectInfoFieldsActivity;
import com.geeksville.location.GPSToPositionWriter;
import com.geeksville.location.LeonardoLiveWriter;
import com.geeksville.location.LocationDBWriter;
import com.geeksville.location.LocationList;
import com.geeksville.location.LocationListWriter;
import com.geeksville.location.PositionWriter;
import com.geeksville.location.PositionWriterSet;
import com.geeksville.view.AsyncProgressDialog;

public class LoggingControl extends ListActivity implements LifeCyclePublisher,
		ChangeHandler {

	/**
	 * Debugging tag
	 */
	private static final String TAG = "LoggingControl";

	private LifeCyclePublisherImpl lifePublish = new LifeCyclePublisherImpl();

	private Button loggingButton;
	private TextView loggingLabel;
	private InfoListView infoView;

	/**
	 * my preferences DB
	 */
	private GagglePrefs prefs;

	// Need handler for callbacks to the UI thread
	private final Handler handler = new Handler();

	static final int INFOSELECT_REQUEST = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flight_main);

		// Log.d(TAG, "onCreate() called");

		prefs = new GagglePrefs(this);

		// Attach to our views

		infoView = (InfoListView) this.getListView();
		loggingButton = (Button) findViewById(R.id.LoggingOnOffButton);
		loggingButton.setOnClickListener(loggingToggle);

		loggingLabel = (TextView) findViewById(R.id.LabelLiveFlight);

		validateAccounts();

		restoreInfoFields();
	}

	/**
	 * Collect app metrics on Flurry
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		// Log.d(TAG, "onStart() called");

		super.onStart();

		FlurryAgent.onStartSession(this, "XBPNNCR4T72PEBX17GKF");
		lifePublish.onStart();
	}

	/**
	 * Collect app metrics on Flurry
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		// Log.d(TAG, "onStop() called");

		super.onStop();

		FlurryAgent.onEndSession(this);
		lifePublish.onStop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();

		saveInfoFields();
		((GaggleApplication) getApplication()).getGpsLogger().setObserver(null);

		// Log.d(TAG, "onPause() called");
		lifePublish.onPause();
	}

	private void saveInfoFields() {
		// Save our list of recipients - FIXME, use the correct android saving
		// system
		try {
			ObjectOutputStream stream = AndroidUtil.writeObjectStream(this,
					"infofields");
			stream.writeObject(infoView.getChecked());
			stream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void restoreInfoFields() {
		try {
			// FIXME - use correct android system
			ObjectInputStream stream = AndroidUtil.readObjectStream(this,
					"infofields");
			infoView.setChecked((String[]) stream.readObject());
			stream.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	protected void onRestart() {
		super.onRestart();

		// Log.d(TAG, "onRestart() called");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		// Log.d(TAG, "onResume() called");
		lifePublish.onResume();

		((GaggleApplication) getApplication()).getGpsLogger().setObserver(this);

		// Restore the toggle to the correct state. FIXME, why doesn't this
		// state get stored automatically by android?
		// Super skanky way to find our possibly not yet existing service
		showLoggingStatus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy() called");

		super.onDestroy();
	}

	private void showLoggingStatus() {
		GPSToPositionWriter gpsToPos = ((GaggleApplication) getApplication())
				.getGpsLogger();

		loggingLabel.setText(gpsToPos.getStatusString());
		loggingButton.setText(gpsToPos.isLogging() ? R.string.stop_logging
				: R.string.start_logging);
	}

	/**
	 * Update our GUI thread because the logging status has changed
	 */
	@Override
	public void onChanged(Object data) {
		handler.post(onLoggingChange);
	}

	private Runnable onLoggingChange = new Runnable() {

		@Override
		public void run() {
			showLoggingStatus();
		}
	};

	private Button.OnClickListener loggingToggle = new Button.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			GPSToPositionWriter gpsToPos = ((GaggleApplication) getApplication())
					.getGpsLogger();

			if (!gpsToPos.isLogging()) {
				if (((GaggleApplication) getApplication())
						.enableGPS(LoggingControl.this)) {

					startLogging();
				}
			} else {
				gpsToPos.stopLogging();
			}
		}

	};

	/**
	 * Create our options menu
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		getMenuInflater().inflate(R.menu.current_flight, menu);

		return true;
	}

	/**
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.customfields:
			Intent intent = new Intent(this, SelectInfoFieldsActivity.class);
			intent.putExtra("checked", infoView.getChecked());
			startActivityForResult(intent, INFOSELECT_REQUEST);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	/**
	 * @see android.app.Activity#onActivityResult(int, int,
	 *      android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == INFOSELECT_REQUEST) {
			if (data != null) {
				String[] checked = data.getStringArrayExtra("checked");
				infoView.setChecked(checked);
			}

			return;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * Check that the username is valid, if not, turn off the appropriate
	 * checkbox
	 */
	private void validateAccounts() {
	}

	private void editAccounts(int acctNum) {
		/*
		 * Intent i = new Intent(this, AccountsActivity.class);
		 * i.putExtra(AccountsActivity.EXTRA_ACCT_NUM, acctNum);
		 * this.startActivity(i);
		 */

		validateAccounts();
	}

	/**
	 * Handle our menu
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		/*
		 * case R.id.accounts_menu: editAccounts(0); return true;
		 */
		}
		return super.onOptionsItemSelected(item);
	}

	private void startLogging() {

		final Account acct = new Account(this, "live2");

		// We always log to the DB
		final PositionWriter dbwriter = new LocationDBWriter(this, prefs
				.isDelayedUpload(), prefs.getPilotName(), null);

		// Also always keep the the current live track
		// FIXME - skanky way we pass live tracks to the map
		LocationList loclist = new LocationList();
		FlyMapActivity.liveList = loclist;

		final PositionWriter ramwriter = new LocationListWriter(loclist);

		loggingButton.setEnabled(false); // Turn off the button until our
		// background thread finishes

		AsyncProgressDialog progress =
				new AsyncProgressDialog(this, getString(R.string.starting_logging),
						getString(R.string.please_wait)) {

					@Override
					protected void doInBackground() {
						PositionWriter[] selected = null;

						// Possibly also leonardo live
						if (prefs.isLiveUpload()) {
							try {
								if (!acct.isValid())
									throw new Exception(
											getString(R.string.username_or_password_is_unset));

								// FIXME - do this in an async dialog helper
								PositionWriter liveWriter = new LeonardoLiveWriter(
										LoggingControl.this, acct.serverURL,
										acct.username, acct.password, prefs.getWingModel(), prefs
												.getLogInterval());

								selected = new PositionWriter[] { dbwriter, ramwriter, liveWriter };
							} catch (Exception ex) {
								// Bad password or connection problems
								showCompletionDialog(context
										.getString(R.string.leonardolive_problem), ex.getMessage());
							}
						}

						// If we haven't already connected to the live server
						if (selected == null)
							selected = new PositionWriter[] { dbwriter, ramwriter };

						PositionWriter writer = new PositionWriterSet(selected);

						// Start up our logger service
						GPSToPositionWriter gpsToPos = ((GaggleApplication) getApplication())
								.getGpsLogger();

						gpsToPos.startLogging(getApplication(), writer, prefs.getLogInterval(),
								prefs.getLaunchDistX(), prefs.getLaunchDistY());
					}

					/**
					 * @see com.geeksville.view.AsyncProgressDialog#onPostExecute
					 *      (java.lang.Void)
					 */
					@Override
					protected void onPostExecute(Void unused) {
						loggingButton.setEnabled(true);

						super.onPostExecute(unused);
					}

				};

		progress.execute();
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
