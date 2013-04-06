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

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.geeksville.gaggle.fragments.AbstractGeeksvilleMapFragment;
import com.geeksville.maps.GeeksvilleMapActivity;

/**
 * Provides structured access for reading our prefs
 * 
 * @author kevinh
 * 
 */
public class GagglePrefs {

    private static final String USE_ONLINE_SOURCE_AS_BACKGROUND_FOR_ARCHIVES = "UseOnlineSourceAsBackgroundForArchives";
	private static final String SELECTED_TILE_SOURCE_NAME = "selectedTileSourceName";
    private static final String SELECTED_ARCHIVES = "selected_archives";
	public final static String mapZoomCenterPref_LAT = "MAP_ZOOM_CENTER_PREF_LAT";
	public final static String mapZoomCenterPref_LON = "MAP_ZOOM_CENTER_PREF_LON";
	public final static String mapZoomCenterPref_ZOOM = "MAP_ZOOM_CENTER_PREF_ZOOM";
	/**
	 * my preferences DB
	 */
	SharedPreferences prefs;
	SharedPreferences.Editor editor;
    private static GagglePrefs instance;

	public GagglePrefs(Context c) {
		prefs = PreferenceManager.getDefaultSharedPreferences(c);
		editor = prefs.edit();
	}

    public static GagglePrefs getInstance() {
        if (instance == null) {
            instance = new GagglePrefs(GaggleApplication.getContext());
        }
        return instance;
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

	public boolean isFlurryEnabled() {
		return prefs.getBoolean("use_flurry_conf", false);
	}

	public boolean isGalaxySLeapYearBugWorkaroundEnabled(){
		return prefs.getBoolean("workaround_sgs_leap_bug", false);
	}

	public boolean useNmeaGeoidInfo(){
		return prefs.getString("altitude_correction", "none").equals("nmea");
	}

	public boolean useEGM84Geoid(){
		return prefs.getString("altitude_correction", "none").equals("egm84");
	}
	public boolean debugAltitudeCorrection(){
		return prefs.getBoolean("altitude_correction_debug", false);
	}

	public int getMapCenterZoom_Lon() {
		return prefs.getInt(mapZoomCenterPref_LON, -1);
	}

	public int getMapCenterZoom_Lat() {
		return prefs.getInt(mapZoomCenterPref_LAT, -1);
	}

	public int getMapCenterZoom_Zoom() {
		return prefs.getInt(mapZoomCenterPref_ZOOM, -1);
	}

	public void setMapCenterZoom(final int lat, final int lon, final int zoom) {
		editor.putInt(mapZoomCenterPref_LAT, lat);
		editor.putInt(mapZoomCenterPref_LON, lon);
		editor.putInt(mapZoomCenterPref_ZOOM, zoom);
		editor.commit();
	}

	public void setInfoFields(final String[] fields){
		StringBuffer sb = new StringBuffer();
		String sep = "";
		for (String s : fields){
			sb.append(sep + s);
			sep=",";
		}
		editor.putString("saved_info_fields", sb.toString());
		editor.commit();
	}

	public String[] getInfoFields(){
		String s = prefs.getString("saved_info_fields", "");
		if (s.equals("")) return new String[0];
		return s.split(",");
	}
	
	public Set<String> getSelectedArchiveFileNames() {
        String archString = prefs.getString(SELECTED_ARCHIVES, "");
        String[] archs = archString.split(",");
        Set<String> returnValue = new HashSet<String>();
        for (String string : archs) {
            if (!string.equals("")) {
                returnValue.add(string);
            }
        }
        return returnValue;
    }

    public void setSelectedArchiveFileNames(Set<String> archiveFileNames) {
        StringBuilder sb = new StringBuilder();
        int counter = 0;
        for (String string : archiveFileNames) {
            sb.append(string);
            if (counter < archiveFileNames.size()-1) {
                sb.append(",");
            }
            counter++;
        }
        Editor edit = prefs.edit();
        edit.putString(SELECTED_ARCHIVES, sb.toString());
        edit.commit();
    }

    public void setSelectedTileSourceName(String name) {
        Editor edit = prefs.edit();
        edit.putString(SELECTED_TILE_SOURCE_NAME, name);
        edit.commit();
    }

    public String getSelectedTileSourceName() {
        return prefs.getString(SELECTED_TILE_SOURCE_NAME, AbstractGeeksvilleMapFragment.Archive.name());
    }

	public boolean getUseOnlineSourceAsBackgroundForArchives() {
		return prefs.getBoolean(USE_ONLINE_SOURCE_AS_BACKGROUND_FOR_ARCHIVES, false);
	}
	public void setUseOnlineSourceAsBackgroundForArchives(boolean useOnlineSourceAsBackgroundForArchives) {
        Editor edit = prefs.edit();
        edit.putBoolean(USE_ONLINE_SOURCE_AS_BACKGROUND_FOR_ARCHIVES, useOnlineSourceAsBackgroundForArchives);
        edit.commit();
	}
}
