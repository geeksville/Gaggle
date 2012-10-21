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
