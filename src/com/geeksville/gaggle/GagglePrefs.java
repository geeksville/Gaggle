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

import java.math.BigInteger;
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
	public int getCompetitionClass() {
		String val = prefs.getString("competition_class_pref", "3").trim();
		return Integer.parseInt(val);
	}
	
	public int getLeonardoLiveVehicleType() {
		String val = prefs.getString("leonardo_live_vehicle_type_pref", "1").trim();
		return Integer.parseInt(val);
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

		public int getLiveLogTimeInterval(){
		String val = prefs.getString("livelog_update_freq_pref", "5");
		return Integer.parseInt(val);
	}

	public long getSkyLinesKey() {
		String val = prefs.getString("skylines_key", "0");
		return new BigInteger(val, 16).longValue();
	}

	public boolean isSkyLinesTrackingEnabled() {
		return prefs.getBoolean("skylines_tracking", false);
	}

	public int getSkyLinesTrackingInterval() {
		String val = prefs.getString("skylines_tracking_interval", "5");
		return Integer.parseInt(val);
	}
	
	public int getLogTimeInterval() {
		String val = prefs.getString("tracklog_update_freq_pref", "5");
		return Integer.parseInt(val);
	}
	
	public float getLogDistanceInterval() {
		String val = prefs.getString("tracklog_update_dist_pref", "100");
		return Float.parseFloat(val);
	}
	public long getScreenUpdateFreq() {
		String val = prefs.getString("screen_update_freq_pref", "5");
		return Integer.parseInt(val);
	}
	public float getScreenUpdateDist() {
		String val = prefs.getString("screen_update_dist_pref", "100");
		return Float.parseFloat(val);
	}
	public int getGPSUpdateFreq() {
		String val = prefs.getString("gps_update_freq_pref", "5");
		return Integer.parseInt(val);
	}
	public float getGPSUpdateDist() {
		String val = prefs.getString("gps_update_dist_pref", "100");
		return Float.parseFloat(val);
	}
	
	
	public int getLaunchDistX() {
		String val = prefs.getString("launch_dist_x", "40");
		if (val.length() == 0)
			return 0;
		else
			return Integer.parseInt(val);
	}

	public int getLaunchDistY() {
		String val = prefs.getString("launch_dist_y", "7");
		if (val.length() == 0)
			return 0;
		else
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

    public boolean isDateOffsetWorkaroundEnabled(){
        return prefs.getBoolean("workaround_1day_offset_bug", false);
    }
}
