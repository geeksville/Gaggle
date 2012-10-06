package com.geeksville.gaggle.fragments;

import com.geeksville.gaggle.R;
import com.geeksville.location.LocationLogDbAdapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Toast;

//implements LoaderCallbacks<Cursor>
//@Override
//public void onLoadFinished(Loader<Cursor> loader, Cursor cursor){
//	myCursor = cursor;
//}
//
//@Override
//public void onLoaderReset(Loader<Cursor> loader){
//	myCursor = null;
//}
//
//@Override
//public Loader<Cursor> onCreateLoader(int id, Bundle args){
//	return null;
//}

public abstract class AbstractDBListFragment 
extends ListFragment  {

	protected Cursor myCursor;
	// / Should the user be shown a confirming dialog
	protected Boolean isConfirmDeletes = true;

	BaseAdapter adapter;

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(getListView());
		myCursor = createCursor();
		getActivity().startManagingCursor(myCursor);

		adapter = createListAdapter();
		setListAdapter(adapter);
	}


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	/**
	 * Collect app metrics on Flurry
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
//		
//	    GagglePrefs prefs = new GagglePrefs(this);
//	    if (prefs.isFlurryEnabled())
//	    	FlurryAgent.onStartSession(this, "XBPNNCR4T72PEBX17GKF");
	}

	/**
	 * Collect app metrics on Flurry
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
//		
//	    GagglePrefs prefs = new GagglePrefs(this);
//	    if (prefs.isFlurryEnabled())
//	    	FlurryAgent.onEndSession(this);
	}


	
	private void doDelete(MenuItem item) {
		if (handleDeleteItem(item)) {
			myCursor.requery(); // We just deleted a
								// row, it seems we need
			// to manually refetch the cursor

			Toast.makeText(getActivity(), R.string.deleted, Toast.LENGTH_SHORT).show();
		}

		// adapter.notifyDataSetChanged(); // this
		// doesn't seem to do
		// anything
	}

	private void confirmDelete(final MenuItem item) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
        super.onCreateContextMenu(menu, v, menuInfo);
		getActivity().getMenuInflater().inflate(R.menu.db_list_activity_context, menu);
	}

//	/**
//	 * Create our options menu
//	 * 
//	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
//	 */
//	@Override
//	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//
//		// FIXME - our menus are not yet ready
//		return super.onCreateOptionsMenu(menu);
//
//		// getMenuInflater().inflate(R.menu.logged_flight_optionmenu, menu);
//		// return true;
//	}

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
