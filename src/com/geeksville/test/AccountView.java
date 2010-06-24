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

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

/**
 * A custom view for editing account information
 * 
 * @author kevinh FIXME - should I instead use the Android AccountManager API
 *         (which I just discovered - doh!)
 */
public class AccountView extends LinearLayout {

	private Account account;

	Spinner serveropts;
	EditText username;
	EditText password;

	String prefsName;
	int serverNamesId, serverURLsId;

	CharSequence[] serverURLs;

	/*
	 * We only support XML based creation for now - because I'm too lazy public
	 * AccountView(Context context) { super(context); }
	 */

	public AccountView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.AccountView);
		serverNamesId = arr.getResourceId(R.styleable.AccountView_server_names, 0);
		serverURLsId = arr.getResourceId(R.styleable.AccountView_server_urls, 0);
		prefsName = arr.getString(R.styleable.AccountView_prefs_name);
		arr.recycle();

		Resources res = getResources();
		if (serverURLsId != 0)
			serverURLs = res.getTextArray(serverURLsId);
		else
			serverURLs = new CharSequence[0]; // Support design time viewing
	}

	/**
	 * The info from this view
	 * 
	 * @return
	 */
	public Account getAccount() {
		return account;
	}

	/**
	 * Add the children from our layout.xml - FIXME, is there a better way to do
	 * this?
	 */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		Context context = getContext();
		((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.account, this);

		serveropts = (Spinner) findViewById(R.id.spinnerServername);
		username = (EditText) findViewById(R.id.editLoginName);
		password = (EditText) findViewById(R.id.editPassword);

		// Restrict username to legal chars - hopefully not too
		InputFilter[] filt = { new UsernameFilter() };
		username.setFilters(filt);
		password.setFilters(filt);

		fillSpinner();
		fillFromPrefs();
	}

	/**
	 * Write current edits to the DB
	 */
	public void write() {
		account.username = username.getText().toString();
		account.password = password.getText().toString();

		int selNum = serveropts.getSelectedItemPosition();
		account.serverURL = serverURLs[selNum].toString();

		account.write();
	}

	void fillSpinner() {
		if (serverNamesId != 0) {
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
					serverNamesId, android.R.layout.simple_spinner_item);

			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			serveropts.setAdapter(adapter);
		}
	}

	void fillFromPrefs() {

		if (prefsName != null)
			account = new Account(getContext(), prefsName);

		username.setText(account.username);
		password.setText(account.password);

		// Super crufty way to find the server name
		for (int i = 0; i < serverURLs.length; i++) {
			String candidate = serverURLs[i].toString();

			if (candidate.equals(account.serverURL)) {
				serveropts.setSelection(i);
				break;
			}
		}
	}
}
