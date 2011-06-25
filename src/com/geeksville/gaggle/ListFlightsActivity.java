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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.flurry.android.FlurryAgent;
import com.geeksville.android.DBListActivity;
import com.geeksville.gaggle.R;
import com.geeksville.location.CSVWriter;
import com.geeksville.location.IGCWriter;
import com.geeksville.location.KMLWriter;
import com.geeksville.location.GPXWriter;
import com.geeksville.location.LeonardoUpload;
import com.geeksville.location.LocationList;
import com.geeksville.location.LocationListWriter;
import com.geeksville.location.LocationLogDbAdapter;
import com.geeksville.location.PositionWriter;
import com.geeksville.location.LocationUtils;
import com.geeksville.view.AsyncProgressDialog;

import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Browse previous flights
 * 
 * @author kevinh
 * 
 */
public class ListFlightsActivity extends DBListActivity {

	/**
	 * Debugging tag
	 */
	private static final String TAG = "ListFlightsActivity";

	private LocationLogDbAdapter db;

	private java.text.DateFormat datefmt;
	private java.text.DateFormat timefmt;
	private Geocoder coder;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		coder = new Geocoder(this);
		datefmt = DateFormat.getDateFormat(this);
		timefmt = DateFormat.getTimeFormat(this);

		// Fill our table
		db = new LocationLogDbAdapter(this);

		setContentView(R.layout.list_main);
		super.onCreate(savedInstanceState);

		WebView view = (WebView) findViewById(android.R.id.empty);
		// view.getSettings().setJavaScriptEnabled(true);
		view.loadUrl("file:///android_asset/no_flights.html");
	}

	/**
	 * Handle our context menu
	 * 
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.logged_upload_menu:
			// FIXME - show a progress dialog
			leonardoUpload(itemToRowId(item));
			return true;
		case R.id.send_igc:
			emailFlight(itemToRowId(item), "igc");
			return true;
		case R.id.send_kml:
			emailFlight(itemToRowId(item), "kml");
			return true;
		case R.id.send_gpx:
			emailFlight(itemToRowId(item), "gpx");
			return true;
		case R.id.send_csv:
			emailFlight(itemToRowId(item), "csv");
			return true;
			// case R.id.view_kml:
			// externalViewFlight(itemToRowId(item), "kml");
			// return true;
		default:
			break;
		}

		return super.onContextItemSelected(item);
	}

	/**
	 * Another flight might have been added, so refresh our cursor
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();

		myCursor.requery();
	}

	/**
	 * 
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu,
	 *      android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		getMenuInflater().inflate(R.menu.logged_flight_context, menu);
	}

	@Override
	protected Cursor createCursor() {
		return db.fetchAllFlights();
	}

	/**
	 * Get a long human readable description for a time
	 * 
	 * @param timeMsec
	 * @return
	 */
	private String timeToString(long timeMsec) {
		Date startdate = new Date(timeMsec);

		String title = datefmt.format(startdate) + " " + timefmt.format(startdate);

		return title;

	}

	/**
	 * Use google to find a human style name for a lat/long (i.e. town name)
	 * 
	 * @author kevinh
	 * 
	 */
	private class FindGeocodeTask extends AsyncTask<Long, Void, String> {
		TextView dest;

		/**
		 * Constructor
		 * 
		 * @param dest
		 *            the view to update when we get a response
		 */
		public FindGeocodeTask(TextView dest) {
			this.dest = dest;
		}

		@Override
		protected String doInBackground(Long... params) {
			try {
				synchronized (coder) { // only one lookup at a time
					long id = params[0];
					Cursor locs = db.fetchLocations(id);
					if (locs.getCount() < 1)
						return null; // No lat longs avail

					double latitude = locs.getDouble(locs
							.getColumnIndex(LocationLogDbAdapter.KEY_LATITUDE));
					double longitude = locs.getDouble(locs
							.getColumnIndex(LocationLogDbAdapter.KEY_LONGITUDE));
					List<Address> addrs = coder.getFromLocation(latitude, longitude, 1);

					if (addrs.size() < 1)
						return null; // Failed to find on google

					Address addr = addrs.get(0);
					StringBuilder builder = new StringBuilder();
					int maxLine = Math.min(addr.getMaxAddressLineIndex(), 1);
					for (int line = 0; line <= maxLine; line++)
						builder.append(addr.getAddressLine(line) + " ");

					String result = builder.toString();

					// Store this as the new description for the flight
					db.updateFlight(id, null, result, null, null);

					return result;
				}
			} catch (Exception ex) {
				// FIXME - log failures
				return null;
			}
		}

		protected void onPostExecute(String result) {
			if (result != null)
				dest.setText(result);
		}
	}

	@Override
	protected BaseAdapter createListAdapter() {

		// Create an array to specify the fields we want to display in the
		// list
		String[] from = new String[] { LocationLogDbAdapter.KEY_DESCRIPTION,
				LocationLogDbAdapter.KEY_FLT_STARTTIME,
				LocationLogDbAdapter.KEY_FLT_ENDTIME };

		// and an array of the fields we want to bind those fields to
		int[] to = new int[] { R.id.flightTitle, R.id.date, R.id.duration };

		// Now create a simple cursor adapter and set it to display
		SimpleCursorAdapter a = new SimpleCursorAdapter(this, R.layout.flights_row,
				myCursor,
				from, to);

		final int idcol = myCursor.getColumnIndex(LocationLogDbAdapter.KEY_ROWID);
		final int desccol = myCursor.getColumnIndex(LocationLogDbAdapter.KEY_DESCRIPTION);
		final int startcol = myCursor.getColumnIndex(LocationLogDbAdapter.KEY_FLT_STARTTIME);
		final int endcol = myCursor.getColumnIndex(LocationLogDbAdapter.KEY_FLT_ENDTIME);

		a.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			public boolean setViewValue(View _view, Cursor cursor, int columnIndex) {
				if (columnIndex == desccol) {
					TextView view = (TextView) _view;

					String desc = cursor.getString(desccol);
					boolean needGeocode = desc == null || desc.length() == 0;
					if (needGeocode) {
						// No user provided description, so we show the date of
						// flight instead
						long startTime = cursor.getLong(startcol);
						desc = timeToString(startTime);

					}
					view.setText(desc);

					if (needGeocode) {
						// Try to find something better next time
						FindGeocodeTask task = new FindGeocodeTask(view);
						task.execute(cursor.getLong(idcol));
					}

					return true;
				}

				if (columnIndex == startcol) {
					TextView view = (TextView) _view;

					long startTime = cursor.getLong(startcol);
					view.setText(timeToString(startTime));
					return true;
				}

				if (columnIndex == endcol) {
					TextView view = (TextView) _view;

					long startTime = cursor.getLong(startcol);
					long endTime = cursor.getLong(endcol);

					// If this flight is still active we won't know the end time
					String str = "";
					if (endTime >= startTime) {
						int numSec = (int) ((endTime - startTime) / 1000);
						int numHr = numSec / (60 * 60);
						int numMin = (numSec / 60) % 60;

						str = String.format("%d:%02d", numHr, numMin);
					}

					view.setText(str);
					return true;
				}

				return false;
			}
		});

		return a;
	}

	/**
	 * 
	 * @see com.geeksville.android.DBListActivity#handleDeleteItem(android.view.MenuItem
	 *      )
	 */
	@Override
	protected boolean handleDeleteItem(MenuItem item) {
		db.deleteFlight(itemToRowId(item));

		return true;
	}

	/**
	 * 
	 * @see com.geeksville.android.DBListActivity#handleViewItem(android.view.MenuItem
	 *      )
	 */
	@Override
	protected void handleViewItem(MenuItem item) {
		viewFlightId(itemToRowId(item));
	}

	/**
	 * Show the flight on the map
	 * 
	 * @param flightid
	 */
	private void viewFlightId(long flightid) {

		// Get all the points in that flight
		LocationList locs = new LocationList();
		PositionWriter locsWriter = new LocationListWriter(locs);
		LocationUtils.dbToWriter(db, locsWriter, flightid);

		Intent i = FlyMapActivity.createIntentLogView(this, locs);
		startActivity(i);
	}

	/**
	 * Write a flight to an in memory stream of IGC data
	 * 
	 * @param flightid
	 * @return
	 * @throws IOException
	 */
	private String flightToIGC(long flightid) throws IOException {
		// File cacheDir = getCacheDir();

		// FileOutputStream s = this.openFileOutput(basename,
		// MODE_WORLD_READABLE);
		ByteArrayOutputStream s = new ByteArrayOutputStream(4096);

		// This will close the file descriptor once done writing
		PositionWriter writer;

		GagglePrefs prefs = new GagglePrefs(this);

		writer = new IGCWriter(s,
				prefs.getPilotName(),
				null, // FIXME - not quite
				// right, we should
				// get this from DB
				prefs.getWingModel(),
				prefs.getPilotId());

		LocationUtils.dbToWriter(db, writer, flightid);

		byte[] contents = s.toByteArray();

		// Super skanky to pass this (big?) stuff as a string, but I'm tired of
		// fighting http post
		return new String(contents);
	}

	/**
	 * Write a flight to a file
	 * 
	 * @param flightid
	 * @param filetype
	 *            igc or kml
	 * @return
	 * @throws IOException
	 */
	private File flightToFile(long flightid, String filetype) throws IOException {
		// File cacheDir = getCacheDir();

		File sdcard = Environment.getExternalStorageDirectory();
		if (!sdcard.exists())
			throw new IOException(getString(R.string.sd_card_not_found));

		String path = getString(R.string.file_folder); 
		File tracklog = new File(sdcard, path);
		if (!tracklog.exists())
			tracklog.mkdir();
		path += '/' + getString(R.string.tracklogs);
		tracklog = new File(sdcard, path);
		if (!tracklog.exists())
			tracklog.mkdir();
		
		String basename = getString(R.string.flight_) + flightid + "." + filetype; // FIXME,
		// use
		// a
		// better
		// filename
		// File fname = new File(cacheDir, basename);
		// FIXME - use getCacheDir - or even better a sdcard directory for
		// flights

		File fullname = new File(tracklog, basename);

		// FileOutputStream s = this.openFileOutput(basename,
		// MODE_WORLD_READABLE);
		FileOutputStream s = new FileOutputStream(fullname);

		// This will close the file descriptor once done writing
		PositionWriter writer;

		GagglePrefs prefs = new GagglePrefs(this);

		if (filetype.equals("igc"))
			writer = new IGCWriter(s,
					prefs.getPilotName(),
					null,
					prefs.getWingModel(),
					prefs.getPilotId());
		else if (filetype.equals("csv"))
			writer = new CSVWriter(s,
					prefs.getPilotName(),
					null,
					prefs.getWingModel(),
					prefs.getPilotId());
		else if (filetype.equals("gpx"))
			writer = new GPXWriter(s,
					prefs.getPilotName(),
					null,
					prefs.getWingModel(),
					prefs.getPilotId());
		else
			writer = new KMLWriter(s,
					prefs.getPilotName(),
					null,
					prefs.getWingModel(),
					prefs.getPilotId());

		LocationUtils.dbToWriter(db, writer, flightid);

		return fullname;

	}

	private void leonardoUpload(final long flightid) {

		final Account acct = new Account(this, "delayed");
		final GagglePrefs gprefs = new GagglePrefs(this);
		
		if (acct.isValid()) {
			AsyncProgressDialog progress =
					new AsyncProgressDialog(this, getString(R.string.uploading),
							getString(R.string.please_wait)) {
						@Override
						protected void doInBackground() {

							String fileLoc;

							try {
								fileLoc = flightToIGC(flightid);
							}
					catch (IOException ex) {
						showCompletionDialog(getString(R.string.igc_stream_write_failed), ex
								.getLocalizedMessage());
						return;
					}

					try {
						String basename = getString(R.string.flight_) + flightid;
						String toastMessage = LeonardoUpload.upload(acct.username, acct.password,
								acct.serverURL, gprefs.getCompetitionClass(), basename,
								fileLoc, acct.connectionTimeout, acct.operationTimeout);
	
						if(toastMessage != null && toastMessage.length() == 0)
							toastMessage = context.getString(R.string.upload_failed_bad_url);
						showCompletionToast(toastMessage);

					} catch (IOException ex) {
						showCompletionDialog(getString(R.string.upload_failed), ex
								.getLocalizedMessage());
					}
				}
					};

			progress.execute();
		} else {
			Toast.makeText(this, R.string.please_set_your_leonardo_account_information,
					Toast.LENGTH_LONG)
					.show();
			startActivity(new Intent(this, MyPreferences.class));
		}
	}

	private class AsyncFileWriter extends AsyncProgressDialog {
		protected File fileLoc = null;
		long flightid;
		String filetype;

		public AsyncFileWriter(final long flightid, final String filetype) {
			super(ListFlightsActivity.this, getString(R.string.writing_file),
					getString(R.string.please_wait));

			this.flightid = flightid;
			this.filetype = filetype;
		}

		@Override
		protected void doInBackground() {

			try {
				fileLoc = flightToFile(flightid, filetype);
			} catch (IOException ex) {
				showCompletionDialog(getString(R.string.file_write_failed), ex
						.getLocalizedMessage());
			}
		}
	}

	/**
	 * email a flight
	 * 
	 * @param flightid
	 * @param filetype
	 *            kml, igc, csv or gpx
	 */
	private void emailFlight(final long flightid, final String filetype) {

		final String filetypeUpper = filetype.toUpperCase();

		AsyncProgressDialog progress =
				new AsyncFileWriter(flightid, filetype) {

					/*
					 * (non-Javadoc)
					 * 
					 * @see
					 * com.geeksville.view.AsyncProgressDialog#onPostExecute
					 * (java.lang .Void)
					 */
					@Override
					protected void onPostExecute(Void unused) {
						super.onPostExecute(unused);

						if (fileLoc != null) {
							Uri fileuri = Uri.fromFile(fileLoc);

							// Intent sendIntent = new
							// Intent(Intent.ACTION_SEND_MULTIPLE);
							Intent sendIntent = new Intent(Intent.ACTION_SEND);
							// Mime type of the attachment (or) u can use
							// sendIntent.setType("*/*")
							if (filetype.equals("igc"))
								sendIntent.setType("application/x-igc");
							else if (filetype.equals("csv"))
								sendIntent.setType("text/csv; header");
							else if (filetype.equals("gpx"))
								sendIntent.setType("application/gpx+xml");
							else
								// FIXME, support kmz
								sendIntent.setType("application/vnd.google-earth.kml+xml");

							// sendIntent.setType("*/*");
							// Subject for the message or Email
							sendIntent.putExtra(Intent.EXTRA_SUBJECT, "My Flight");

							// Full Path to the attachment
							// ArrayList<Uri> files = new ArrayList<Uri>();
							// files.add(fileuri);
							// sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,
							// files);
							sendIntent.putExtra(Intent.EXTRA_STREAM, fileuri);

							// Use a chooser to decide whether email or mms
							startActivity(Intent.createChooser(sendIntent, "Send flight"));

							// Keep stats on # of emails sent
							Map<String, String> map = new HashMap<String, String>();
							map.put("Time", (new Date()).toGMTString());
							FlurryAgent.onEvent("EmailFlight", map);
						}
					}

				};

		progress.execute();
	}

	/**
	 * view a flight in google earth - FIXME, doesn't yet work - need to check
	 * their manifest
	 * 
	 * @param flightid
	 * @param filetype
	 *            kml or igc
	 */
	private void externalViewFlight(final long flightid, final String filetype) {

		final String filetypeUpper = filetype.toUpperCase();

		AsyncProgressDialog progress =
				new AsyncFileWriter(flightid, filetype) {

					/*
					 * (non-Javadoc)
					 * 
					 * @see
					 * com.geeksville.view.AsyncProgressDialog#onPostExecute
					 * (java.lang .Void)
					 */
					@Override
					protected void onPostExecute(Void unused) {
						super.onPostExecute(unused);

						if (fileLoc != null) {
							Uri fileuri = Uri.fromFile(fileLoc);

							// Intent sendIntent = new
							// Intent(Intent.ACTION_SEND_MULTIPLE);
							Intent sendIntent = new Intent(Intent.ACTION_VIEW, fileuri);
							// Mime type of the attachment (or) u can use
							// sendIntent.setType("*/*")
							if (filetype.equals("igc"))
								sendIntent.setType("application/x-igc");
							else
								// FIXME, support kmz
								sendIntent.setType("application/vnd.google-earth.kml+xml");

							startActivity(Intent.createChooser(sendIntent,
									getString(R.string.view_flight)));

							// Keep stats on # of emails sent
							Map<String, String> map = new HashMap<String, String>();
							map.put("Time", (new Date()).toGMTString());
							FlurryAgent.onEvent("GEarthView", map);
						}
					}

				};

		progress.execute();
	}

	/**
	 * Handle clicks on an individual flight log
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		// Find the flight ID the user selected
		long flightid = rowToRowId(position);

		viewFlightId(flightid);
	}
}
