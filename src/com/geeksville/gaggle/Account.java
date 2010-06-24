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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Account/server information
 * 
 * @author kevinh
 * 
 */
public class Account {
 
	public String username;
	public String password;
	public String serverURL;

	private SharedPreferences prefs;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            Someday if we wanted to live without android we should remove
	 *            this
	 * @param prefsName
	 */
	public Account(Context context, String prefsName) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);

		if (prefs == null)
			return; // FIXME - still need to know how to detect eclipse

		username = prefs.getString(prefsName + "_username_pref", "").trim();
		password = prefs.getString(prefsName + "_password_pref", "").trim();
		serverURL = prefs.getString(prefsName + "_servername_pref", "");
	}

	/**
	 * 
	 * @return true if the username and passwords are not empty
	 */
	public boolean isValid() {
		return username.length() != 0 && password.length() != 0 && serverURL.length() != 0;
	}

	/**
	 * Dump our current state to prefs
	 */
	public void write() {
		SharedPreferences.Editor edit = prefs.edit();

		edit.putString("username", username);
		edit.putString("password", password);
		edit.putString("servername", serverURL);

		edit.commit();
	}
}
