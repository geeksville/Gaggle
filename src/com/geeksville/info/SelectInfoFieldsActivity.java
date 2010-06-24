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
package com.geeksville.info;

import com.geeksville.android.LifeCycleHandler;
import com.geeksville.android.LifeCyclePublisher;
import com.geeksville.android.LifeCyclePublisherImpl;
import com.geeksville.gaggle.R;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

/**
 * Allows selecting the contents of InfoDockListView
 * 
 * @author kevinh
 * 
 */
public class SelectInfoFieldsActivity extends ListActivity implements LifeCyclePublisher {

	private InfoListView lv;

	private LifeCyclePublisherImpl lifePublish = new LifeCyclePublisherImpl();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.select_infofields);

		lv = (InfoListView) findViewById(android.R.id.list);
		lv.setShowCheckmarks(true);
		lv.setShowUncheckedRows(true);

		String[] checked = getIntent().getStringArrayExtra("checked");

		lv.setChecked(checked);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();

		lifePublish.onPause();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();

		lifePublish.onResume();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();

		lifePublish.onStart();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();

		lifePublish.onStop();
	}

	/**
	 * Handle the back key - though I'd have preferred to do it in onBackPressed
	 * that doesn't exist in old releases
	 * 
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK)
			return true;

		return super.onKeyDown(keyCode, event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onKeyUp(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// Pass our results back to any caller
			Intent result = new Intent();
			result.putExtra("checked", lv.getChecked());
			setResult(RESULT_OK, result);
			finish();

			return true;
		}

		return super.onKeyUp(keyCode, event);
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
