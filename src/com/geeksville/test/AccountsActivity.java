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
package com.geeksville.test;

import com.flurry.android.FlurryAgent;

import android.app.Activity;
import android.os.Bundle;


public class AccountsActivity extends Activity {

	/**
	 * Extra data we look for in our Intent. Which subview should we select by
	 * default
	 */
	public static final String EXTRA_ACCT_NUM = "acctnum";

	AccountView liveAccount, delayedAccount;

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

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_activity);

		liveAccount = (AccountView) findViewById(R.id.AccountViewLive);
		delayedAccount = (AccountView) findViewById(R.id.AccountViewDelayedUpload);

		Bundle extras = getIntent().getExtras();
		int acctNum = extras.getInt(EXTRA_ACCT_NUM, 0);
		if (acctNum != 0)
			delayedAccount.requestFocus();
	}

	/**
	 * Save our state
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		// FIXME - save more state
		super.onPause();

		liveAccount.write();
		delayedAccount.write();
	}
}
