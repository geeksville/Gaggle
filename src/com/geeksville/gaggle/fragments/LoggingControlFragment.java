package com.geeksville.gaggle.fragments;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.geeksville.android.ChangeHandler;
import com.geeksville.android.LifeCycleHandler;
import com.geeksville.android.LifeCyclePublisher;
import com.geeksville.android.LifeCyclePublisherImpl;
import com.geeksville.gaggle.Account;
import com.geeksville.gaggle.GaggleApplication;
import com.geeksville.gaggle.GagglePrefs;
import com.geeksville.gaggle.R;
import com.geeksville.info.InfoListView;
import com.geeksville.info.SelectInfoFieldsActivity;
import com.geeksville.location.BarometerClient;
import com.geeksville.location.GPSClient;
import com.geeksville.location.GPSToPositionWriter;
import com.geeksville.location.LeonardoLiveWriter;
import com.geeksville.location.LocationDBWriter;
import com.geeksville.location.LocationList;
import com.geeksville.location.LocationListWriter;
import com.geeksville.location.PositionWriter;
import com.geeksville.location.PositionWriterSet;
import com.geeksville.view.AsyncProgressDialog;

public class LoggingControlFragment extends ListFragment implements
		LifeCyclePublisher, ChangeHandler {

	/**
	 * Debugging tag
	 */
	private static final String TAG = "LoggingControlFragment";

	private LifeCyclePublisherImpl lifePublish = new LifeCyclePublisherImpl();

	private Button loggingButton;
	private TextView loggingLabel;

	/**
	 * my preferences DB
	 */
	private GagglePrefs prefs;

	// Need handler for callbacks to the UI thread
	private final Handler handler = new Handler();
 
	static final int INFOSELECT_REQUEST = 0;

	/** Called when the fragment is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate fragment!");
	}

	@Override
	public void onAttach(Activity activity) {
		prefs = new GagglePrefs(activity);
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// super.onCreate(savedInstanceState);

		// setContentView(R.layout.flight_main);
		View v = inflater.inflate(R.layout.loggingcontrol_fragment, container, false);

		loggingButton = (Button) v.findViewById(R.id.LoggingOnOffButton);
		loggingButton.setOnClickListener(loggingToggle);
		loggingLabel = (TextView) v.findViewById(R.id.LabelLiveFlight);

//		restoreInfoFields();
		// Log.d(TAG, "onCreate() called");
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
		validateAccounts();
		restoreInfoFields();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	public void onPause() {
		super.onPause();

		saveInfoFields();
		((GaggleApplication) getActivity().getApplication()).getGpsLogger()
				.setObserver(null);
		lifePublish.onPause();
	}
//
	private void saveInfoFields() {
		InfoListView infoView = (InfoListView) getListView();
		prefs.setInfoFields(infoView.getChecked());
	}

	private void restoreInfoFields() {
//		try {
//			// FIXME - use correct android system
//			ObjectInputStream stream = AndroidUtil.readObjectStream(
//					getActivity(), "infofields");
		final String[] ss = prefs.getInfoFields();
		if (ss.length > 0){
			InfoListView infoView = (InfoListView) getListView();
			infoView.setChecked(ss);
		}
//			stream.close();
//		} catch (Exception e) {
//			Log.e(TAG, "Error restoring info fields", e);
//		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		// Log.d(TAG, "onResume() called");
		lifePublish.onResume();

		((GaggleApplication) getActivity().getApplication()).getGpsLogger()
				.setObserver(this);

		// Restore the toggle to the correct state. FIXME, why doesn't this
		// state get stored automatically by android?
		// Super skanky way to find our possibly not yet existing service
		showLoggingStatus();
	}

	private void showLoggingStatus() {
		GPSToPositionWriter gpsToPos = ((GaggleApplication) getActivity()
				.getApplication()).getGpsLogger();

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
			GPSToPositionWriter gpsToPos = ((GaggleApplication) getActivity()
					.getApplication()).getGpsLogger();

			if (!gpsToPos.isLogging()) {
				if (((GaggleApplication) getActivity().getApplication())
						.enableGPS(LoggingControlFragment.this.getActivity())) {

					startLogging();
				}
			} else {
				gpsToPos.stopLogging();
			}
		}

	};

	/**
	 * Create our options menu
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.current_flight, menu);

		boolean showAltButton = false;
		try {
			showAltButton = BarometerClient.create(getActivity()) != null
					&& GPSClient.instance != null
					&& GPSClient.instance.getLastKnownLocation() != null
					&& GPSClient.instance.getLastKnownLocation().hasAltitude();
		} catch (VerifyError ex) {
			Log.e(TAG, "Not on 1.5: " + ex);
		}
		menu.findItem(R.id.setAltFromGPS).setVisible(showAltButton);

		super.onCreateOptionsMenu(menu, inflater);
	}

	/**
	 * @see android.app.Activity#onActivityResult(int, int,
	 *      android.content.Intent)
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == INFOSELECT_REQUEST) {
			if (data != null) {
				String[] checked = data.getStringArrayExtra("checked");
				InfoListView infoView = (InfoListView) getListView();
				infoView.setChecked(checked);
			}
			saveInfoFields();
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
		case R.id.customfields:
			Intent intent = new Intent(getActivity(), SelectInfoFieldsActivity.class);
			InfoListView infoView = (InfoListView) getListView();
			intent.putExtra("checked", infoView.getChecked());
			startActivityForResult(intent, INFOSELECT_REQUEST);
			return true;
		case R.id.setAltFromGPS:
			// FIXME - http://blueflyvario.blogspot.com/2011_05_01_archive.html
			Location loc = GPSClient.instance.getLastKnownLocation();
			BarometerClient.create(getActivity()).setAltitude(
					(float) loc.getAltitude());
			return true;
			/*
			 * case R.id.accounts_menu: editAccounts(0); return true;
			 */
		}
		return super.onOptionsItemSelected(item);
	}

//	/**
//	 * Handle clicks on an individual waypoint
//	 */
//	@Override
//	public void onListItemClick(ListView l, View v, int position, long id) {
//		//super.onListItemClick(l, v, position, id);
//	}

	private void startLogging() {

		final Account acct = new Account(getActivity(), "live2");

		// We always log to the DB
		final PositionWriter dbwriter = new LocationDBWriter(getActivity(),
				prefs.isDelayedUpload(), prefs.getPilotName(), null);

		// Also always keep the the current live track
		// FIXME - skanky way we pass live tracks to the map
		LocationList loclist = new LocationList();
		FlyMapFragment.liveList = loclist;

		final PositionWriter ramwriter = new LocationListWriter(loclist);

		loggingButton.setEnabled(false); // Turn off the button until our
		// background thread finishes

		AsyncProgressDialog progress = new AsyncProgressDialog(getActivity(),
				getString(R.string.starting_logging),
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
								LoggingControlFragment.this.getActivity(),
								acct.serverURL, acct.username, acct.password,
								prefs.getWingModel(),
								prefs.getLeonardoLiveVehicleType(),
								prefs.getLiveLogTimeInterval());

						selected = new PositionWriter[] { dbwriter, ramwriter,
								liveWriter };
					} catch (Exception ex) {
						// Bad password or connection problems
						showCompletionDialog(
								LoggingControlFragment.this.getActivity()
										.getString(R.string.leonardolive_problem),
								ex.getMessage());
					}
				}

				// If we haven't already connected to the live server
				if (selected == null)
					selected = new PositionWriter[] { dbwriter, ramwriter };

				PositionWriter writer = new PositionWriterSet(selected);

				// Start up our logger service
				GPSToPositionWriter gpsToPos = ((GaggleApplication) getActivity()
						.getApplication()).getGpsLogger();

				gpsToPos.startLogging(getActivity().getApplication(), writer,
						prefs.getLogTimeInterval(), prefs.getLaunchDistX(),
						prefs.getLaunchDistY());
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
