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
	public int connectionTimeout;
	public int operationTimeout;
	
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

		connectionTimeout = Integer.parseInt(prefs.getString(prefsName + "_connection_timeout_pref", "3")) * 1000;
		operationTimeout = Integer.parseInt(prefs.getString(prefsName + "_operation_timeout_pref", "30")) * 1000;
		// FIXME - the Timeout values are not yet used by the leonardo live server code.
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
		edit.putInt("connectionTimeout", connectionTimeout);
		edit.putInt("operationTimeout", operationTimeout);
		edit.commit();
	}
}
