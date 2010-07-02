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
 * Provides structured access for reading our prefs
 * 
 * @author kevinh
 * 
 */
public class GagglePrefs {

	/**
	 * my preferences DB
	 */
	SharedPreferences prefs;

	public GagglePrefs(Context c) {

		prefs = PreferenceManager.getDefaultSharedPreferences(c);
	}

	public String getWingModel() {
		return prefs.getString("wing_model_pref", "").trim();
	}

	public String getPilotId() {
		return prefs.getString("pilot_id_pref", "").trim();
	}

	public boolean isLiveUpload() {
		return prefs.getBoolean("live_logging_pref", false);
	}

	public int getLogInterval() {
		String val = prefs.getString("log_interval_pref", "5");
		return Integer.parseInt(val);
	}

	public int getLaunchDistX() {
		String val = prefs.getString("launch_dist_x", "40");
		return Integer.parseInt(val);
	}

	public int getLaunchDistY() {
		String val = prefs.getString("launch_dist_y", "7");
		return Integer.parseInt(val);
	}

	public boolean isDelayedUpload() {
		return prefs.getBoolean("delayed_logging_pref", false);
	}

	public boolean isKeepScreenOn() {
		return prefs.getBoolean("force_screen_on", false);
	}

	public String getPilotName() {
		return prefs.getString("pilot_name_pref", "").trim();
	}

}
