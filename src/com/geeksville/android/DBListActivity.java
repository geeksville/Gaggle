/****************************************************************************************
 * Gaggle is Copyright 2010, 2011, and 2012 by Kevin Hester of Geeksville Industries LLC,
 * a California limited liability corporation. 
 * 
 * Gaggle is free software: you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation, either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * Gaggle is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE.  See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with Gaggle 
 * included in this distribution in the manual (assets/manual/gpl-v3.txt). If not, see  
 * <http://www.gnu.org/licenses/> or at <http://gplv3.fsf.org>.
 ****************************************************************************************/
package com.geeksville.android;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.geeksville.gaggle.R;
import com.geeksville.location.LocationLogDbAdapter;

public abstract class DBListActivity extends ListActivity {

	protected Cursor myCursor;

	// / Should the user be shown a confirming dialog
	protected Boolean isConfirmDeletes = true;

	BaseAdapter adapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		myCursor = createCursor();
		startManagingCursor(myCursor);

		adapter = createListAdapter();
		setListAdapter(adapter);

		// Turn on the context menu
		registerForContextMenu(getListView());
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

		FlurryAgent.onStartSession(this, "XBPNNCR4T72PEBX17GKF");
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

		FlurryAgent.onEndSession(this);
	}

	private void doDelete(MenuItem item) {
		if (handleDeleteItem(item)) {
			myCursor.requery(); // We just deleted a
								// row, it seems we need
			// to manually refetch the cursor

			Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show();
		}

		// adapter.notifyDataSetChanged(); // this
		// doesn't seem to do
		// anything
	}

	private void confirmDelete(final MenuItem item) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.confirm_delete_)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								doDelete(item);
							}
						})
				.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	/**
	 * Handle our context menu
	 * 
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.delete_menu:
			if (isConfirmDeletes)
				confirmDelete(item);
			else
				doDelete(item);
			return true;

		case R.id.view_menu:
			handleViewItem(item);
			return true;

		default:
			break;
		}

		return super.onContextItemSelected(item);
	}

	/**
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu,
	 *      android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		getMenuInflater().inflate(R.menu.db_list_activity_context, menu);
	}

	/**
	 * Create our options menu
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// FIXME - our menus are not yet ready
		return super.onCreateOptionsMenu(menu);

		// getMenuInflater().inflate(R.menu.logged_flight_optionmenu, menu);
		// return true;
	}

	/**
	 * Given a row num, return the db id for that row
	 * 
	 * @param rowNum
	 * @return
	 * 
	 */
	protected long rowToRowId(int rowNum) {
		myCursor.moveToPosition(rowNum);

		long flightid = myCursor.getLong(myCursor
				.getColumnIndexOrThrow(LocationLogDbAdapter.KEY_ROWID));

		return flightid;
	}

	/**
	 * Find the row number for the selected context menu item
	 * 
	 * @param item
	 * @return
	 */
	protected int itemToRowNum(MenuItem item) {

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();

		return info.position;

	}

	/**
	 * Find the flight id for the selected context menu item
	 * 
	 * @param item
	 * @return
	 */
	protected long itemToRowId(MenuItem item) {

		return rowToRowId(itemToRowNum(item));
	}

	/**
	 * Generate a cursor with data for this view
	 * 
	 * @return
	 */
	protected abstract Cursor createCursor();

	/**
	 * Generate the adapter for viewing our cursor
	 * 
	 * @return
	 */
	protected abstract BaseAdapter createListAdapter();

	/**
	 * Called when the user wants to delete a row
	 * 
	 * @param item
	 * @return true if we did the deletion and should show the user a Toast
	 */
	protected abstract boolean handleDeleteItem(MenuItem item);

	/**
	 * Called when the user wants to view a row
	 * 
	 * @param item
	 */
	protected abstract void handleViewItem(MenuItem item);
}
