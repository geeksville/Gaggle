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
package com.geeksville.gaggle.fragments;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Observable;
import java.util.Observer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.geeksville.android.AndroidUtil;
import com.geeksville.android.DBListActivity;
import com.geeksville.gaggle.GaggleApplication;
import com.geeksville.gaggle.GagglePrefs;
import com.geeksville.gaggle.R;
import com.geeksville.gaggle.WaypointDialog;
import com.geeksville.info.Units;
import com.geeksville.io.LineEndingStream;
import com.geeksville.location.ExtendedWaypoint;
import com.geeksville.location.GPSClientStub;
import com.geeksville.location.LocationLogDbAdapter;
import com.geeksville.location.WPTImporter;
import com.geeksville.location.Waypoint;
import com.geeksville.location.WaypointCursor;
import com.geeksville.location.WaypointDB;
import com.geeksville.view.AsyncProgressDialog;

public class ListWaypointsFragment extends AbstractDBListFragment implements Observer {

	private WaypointDB db;
	private GPSClientStub gps;
	private static final String TAG = "ListWaypointsFragment";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.listwaypoints_fragment, container, false);

		WebView view = (WebView) v.findViewById(android.R.id.empty);
		// view.getSettings().setJavaScriptEnabled(true);
		view.loadUrl(getResources().getString(R.string.no_waypoints_url));
		return v;
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		isConfirmDeletes = false; // Deleting a waypoint isn't such a big deal -
									// don't require confirm

		db = ((GaggleApplication) getActivity().getApplication()).getWaypoints();

		super.onCreate(savedInstanceState);
		perhapsAddFromUri();
	}

	/**
	 * @see android.app.Activity#onPause()
	 */
	@Override
	public void onPause() {
		if (db != null)
			db.deleteObserver(this);

		gps.close();

		super.onPause();
	}

	/**
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();

		Units.instance.setFromPrefs(getActivity());

		gps = new GPSClientStub(getActivity());

		// FIXME, close the backing DB when the waypoint cache is done with it
		db.addObserver(this);
	}

//	@Override
//	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//		return null;
//	}

	/**
	 * Create our options menu
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.waypoint_optionmenu, menu);
	}

	/**
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.add_menu:
			handleAddWaypoint();
			return true;

		case R.id.delete_menu:
			handleDeleteMenu();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu,
	 *      android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		getActivity().getMenuInflater().inflate(R.menu.waypoint_context, menu);
	}

	/**
	 * Handle our context menu
	 * 
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.goto_menu:
			handleGotoWaypoint(item);
			return true;

		default:
			break;
		}

		return super.onContextItemSelected(item);
	}

	/**
	 * Handle clicks on an individual waypoint
	 */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		handleViewItem(position);
	}

	/**
	 * See if the user wants us to open an IGC file FIXME - confirm with user
	 */
	private void perhapsAddFromUri() {
		// FIXME check this is correct !
		Intent intent = getActivity().getIntent();
		String typ = intent.getType();
		final Uri uri = intent.getData();
		String action = intent.getAction();
		if (uri != null && action != null && action.equals(Intent.ACTION_VIEW)) {
			Log.d("ListWaypointsActivity", "Considering " + typ);
			AsyncProgressDialog progress = new AsyncProgressDialog(getActivity(),
					getString(R.string.importing_waypoints),
					getString(R.string.please_wait)) {
				@Override
				protected void doInBackground() {
					GagglePrefs prefs = new GagglePrefs(getActivity());
					if (prefs.isFlurryEnabled())
					  FlurryAgent.onEvent("WPT import start");

					// See if we can read the file
					try {
						InputStream s = AndroidUtil.getFromURI(
								getActivity(), uri);

						WPTImporter imp = new WPTImporter(db);
						int numadded = imp.addFromStream(s);
						if (numadded > 0) {// Save file locally.
							waypointsToFile(imp.fileContents,
									uri.getLastPathSegment());
						}

						String msg = String.format(
								getString(R.string.imported_d_waypoints),
								numadded);

						showCompletionToast(msg);
						if (prefs.isFlurryEnabled())
						  FlurryAgent.onEvent("WPT import success");
					} catch (Exception ex) {
						if (prefs.isFlurryEnabled())
						  FlurryAgent.onEvent("WPT import failed");

						showCompletionDialog(getString(R.string.import_failed),
								ex.getLocalizedMessage());
					}
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see
				 * com.geeksville.view.AsyncProgressDialog#onPostExecute(java
				 * .lang.Void)
				 */
				@Override
				protected void onPostExecute(Void unused) {
					super.onPostExecute(unused);

					if (isShowingDialog()){
						// FIXME we should not finish parent activity.
						// finish(); // Exit our activity - go back to the
					// webserver because we failed
					} else{
						myCursor.requery();
					}
					// FIXME we should not finish parent activity.
					// finish();
				}
			};
			progress.execute();
		}
	}

	private boolean waypointsToFile(String waypoints, String filename)
			throws IOException {
		PrintStream out = null;
		try {
			if (!Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED))
				return false;
			File sdcard = Environment.getExternalStorageDirectory();
			if (!sdcard.exists())
				return false;

			String path = getString(R.string.file_folder);
			File directory = new File(sdcard, path);
			if (!directory.exists())
				directory.mkdir();
			path += '/' + getString(R.string.waypoints);
			directory = new File(sdcard, path);
			if (!directory.exists())
				directory.mkdir();
			String basename = filename;
			File fullname = new File(directory, basename);

			FileOutputStream s = new FileOutputStream(fullname);
			out = new PrintStream(new LineEndingStream(s));
			out.print(waypoints);
		} finally {
			out.close();
		}
		return true;

	}

	/**
	 * @see com.geeksville.android.DBListActivity#createCursor()
	 */
	@Override
	protected Cursor createCursor() {
		Cursor c = db.fetchWaypointsByDistance();

		if (c.getCount() > 0)
			// If we have any points, encourage the user to turn on the GPS so
			// we can show distance
			((GaggleApplication) getActivity().getApplication()).enableGPS(getActivity());

		return c;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.geeksville.android.DBListActivity#createListAdapter()
	 */
	@Override
	protected BaseAdapter createListAdapter() {
		// Create an array to specify the fields we want to display in the
		// list
		String[] from = new String[] { LocationLogDbAdapter.KEY_NAME,
				LocationLogDbAdapter.KEY_DESCRIPTION,
				WaypointCursor.KEY_DIST_PILOTX,
				LocationLogDbAdapter.KEY_WAYPOINT_TYPE,
				WaypointCursor.KEY_DIST_PILOTY };

		// and an array of the fields we want to bind those fields to
		int[] to = new int[] { R.id.name, R.id.description, R.id.distance,
				R.id.image, R.id.units };

		// Now create a simple cursor adapter and set it to display
		SimpleCursorAdapter a = new SimpleCursorAdapter(getActivity(),
				R.layout.waypoint_row, myCursor, from, to);

		final int distcol = myCursor
				.getColumnIndex(WaypointCursor.KEY_DIST_PILOTX);
		final int distycol = myCursor
				.getColumnIndex(WaypointCursor.KEY_DIST_PILOTY);
		final int typecol = myCursor
				.getColumnIndex(LocationLogDbAdapter.KEY_WAYPOINT_TYPE);

		a.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			public boolean setViewValue(View _view, Cursor cursor,
					int columnIndex) {

				try {
					// Show the distance with correct units
					if (columnIndex == distcol) {
						TextView view = (TextView) _view;

						int dist = cursor.getInt(distcol);

						// view.setVisibility(dist == -1 ? View.INVISIBLE :
						// View.VISIBLE);
						String distStr = (dist == -1) ? "---" : Units.instance
								.metersToDistance(dist);
						view.setText(distStr);

						return true;
					}

					// We don't care about pilot y, but this is a skanky hack to
					// set the units view string
					if (columnIndex == distycol) {
						TextView view = (TextView) _view;
						view.setText(Units.instance.getDistanceUnits());
						return true;
					}

					// Show the distance with correct units
					if (columnIndex == typecol) {
						ImageView view = (ImageView) _view;

						ExtendedWaypoint w = ((WaypointCursor) cursor)
								.getWaypoint();

						view.setImageDrawable(w.getIcon());
						return true;
					}

				} catch (RuntimeException ex) {
					Log.d("ListWaypoint",
							"Caught exception building waypoint list: "
									+ ex.getMessage());
				}

				return false;
			}
		});

		return a;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.geeksville.android.DBListActivity#handleDeleteItem(android.view.MenuItem
	 * )
	 */
	@Override
	protected boolean handleDeleteItem(MenuItem item) {
		db.deleteWaypoint(itemToRowId(item));

		return true;
	}

	/**
	 * Delete all waypoints
	 */
	private void handleDeleteMenu() {
		// Is the user sure?
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.delete_waypoints);
		builder.setMessage(R.string.are_you_sure);
		builder.setNegativeButton(R.string.cancel, null);
		builder.setPositiveButton(R.string.delete, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				db.deleteAllWaypoints();

				myCursor.requery();
			}
		});

		AlertDialog alert = builder.create();
		alert.show();
	}

	private void handleAddWaypoint() {

		GaggleApplication app = ((GaggleApplication) getActivity().getApplication());
		Location myloc = null;

		if (gps != null && gps.get() != null)
			myloc = gps.get().getLastKnownLocation();

		if (myloc == null)
			Toast.makeText(getActivity(),
					R.string.can_not_add_waypoint_still_waiting_for_gps_fix,
					Toast.LENGTH_LONG).show();
		else {
			java.util.Date now = new java.util.Date();
			String name = DateFormat.format("yy/MM/dd kk:mm:ss", now)
					.toString();

			ExtendedWaypoint w = new ExtendedWaypoint(name,
					myloc.getLatitude(), myloc.getLongitude(),
					(int) myloc.getAltitude(), 0,
					Waypoint.Type.Unknown.ordinal());
			app.getWaypoints().add(w);

			myCursor.requery();

			// FIXME - then select it in the cursor/GUI
			Toast.makeText(getActivity(),
					R.string.waypoint_created, Toast.LENGTH_SHORT).show();
		}
	}

	private void handleGotoWaypoint(MenuItem item) {
		gotoWaypoint(itemToRowNum(item));
	}

	private void gotoWaypoint(int rownum) {
		myCursor.moveToPosition(rownum);

		final ExtendedWaypoint w = ((WaypointCursor) myCursor).getWaypoint();

		((GaggleApplication) getActivity().getApplication()).currentDestination = w;

		Toast.makeText(getActivity(),
				getString(R.string.new_destination) + w.name,
				Toast.LENGTH_SHORT).show();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.geeksville.android.DBListActivity#handleViewItem(android.view.MenuItem
	 * )
	 */
	@Override
	protected void handleViewItem(MenuItem item) {
		handleViewItem(itemToRowNum(item));
	}

	private void handleViewItem(final int rownum) {
		myCursor.moveToPosition(rownum);

		final ExtendedWaypoint w = ((WaypointCursor) myCursor).getWaypoint();

		Runnable onOkay = new Runnable() {

			@Override
			public void run() {
				w.commit();
				myCursor.requery();

				Toast.makeText(getActivity(),
						R.string.updated_waypoint, Toast.LENGTH_SHORT).show();
			}
		};

		Runnable onGoto = new Runnable() {

			@Override
			public void run() {
				// Just in case the user changed the waypoint
				w.commit();
				myCursor.requery();

				gotoWaypoint(rownum);
			}
		};

		WaypointDialog d = new WaypointDialog(getActivity(), w, onOkay, onGoto);
		d.show();
	}

	@Override
	public void update(Observable observable, Object data) {
		myCursor.requery(); // FIXME - don't do this if the user is moving
		// around?
	}

}
