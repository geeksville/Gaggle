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
package com.geeksville.gaggle;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

/**
 * Show a splashscreen that checks for a password
 * 
 * @author kevinh
 * 
 */
public class BetaSplashActivity extends Activity {
	EditText password;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.beta_splash);

		password = (EditText) findViewById(R.id.edit_beta_password);
		password.setOnKeyListener(klisten);

		if (isExpired()) {
			// If this app has expired, don't let the user do anything
			// FIXME - direct them to the store and store this string in a
			// resource
			TextView label = (TextView) findViewById(R.id.label_beta_text);

			label
					.setText("The beta test period for this application has expired, please visit the Android Market to download a newer version");
			password.setVisibility(TextView.GONE);
		}
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

	private OnKeyListener klisten = new OnKeyListener() {

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			validate();
			return false;
		}

	};

	void validate() {
		boolean good = password.getText().toString().trim().toLowerCase().equals("raptor");

		if (good) {
			SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(this).edit();

			e.putBoolean("is_beta_okay", true);
			e.commit();

			setResult(RESULT_OK);
			finish();
		}
	}

	/**
	 * Is the user allowed to run this app?
	 * 
	 * @return
	 */
	private static boolean isOkay(Context context) {

		return true;

		// return
		// PreferenceManager.getDefaultSharedPreferences(context).getBoolean("is_beta_okay",
		// false);
	}

	/**
	 * Has this executable expired (i.e. user must download an update?)
	 * 
	 * @return
	 */
	private static boolean isExpired() {
		Date now = new Date();

		// FIXME - pull this date from a prefs entry
		Date expireDate = new Date(2010, 8, 1);

		return now.after(expireDate);
	}

	/**
	 * The int code we use for beta requests (FIXME, store in one common place)
	 */
	private static final int REQUEST_CODE = 4403;

	public static void perhapsSplash(Activity context) {
		if (!isOkay(context) || isExpired()) {
			Intent i = new Intent(context, BetaSplashActivity.class);

			context.startActivityForResult(i, REQUEST_CODE);
		}
	}

	/**
	 * If this is a result from our beta screen, handle it correctly (probably
	 * by terminating the app)
	 * 
	 * @param requestCode
	 * @param resultCode
	 */
	public static void handleActivityResult(Activity parent, int requestCode, int resultCode) {
		if (requestCode == REQUEST_CODE && resultCode != RESULT_OK)
			parent.finish();
	}
}
