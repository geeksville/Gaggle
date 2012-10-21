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
