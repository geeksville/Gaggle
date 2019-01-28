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
package com.geeksville.android;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

public class PreferenceUtil {

  private static final String TAG = "PreferenceUtil";

  /**
   * Read prefs string, but gracefully ignore malformatted strings
   * 
   * @param context
   * @param prefsname
   * @param defaultValue
   * @return
   */
  public static float getFloat(Context context, String prefsname,
      float defaultValue) {
    try {
      return PreferenceManager.getDefaultSharedPreferences(context).getFloat(
          prefsname, defaultValue);
    } catch (Exception ex) {
      try {
        // Try to convert it ourselves (in case it was stored as a
        // string)
        return Float.parseFloat(PreferenceManager.getDefaultSharedPreferences(
            context).getString(prefsname, Float.toString(defaultValue)));
      } catch (Exception e) {
        Log.w(TAG, "Ignoring malformed preference: " + prefsname);

        return defaultValue;
      }
    }
  }

  /**
   * Read prefs string, but gracefully ignore malformatted strings
   * 
   * @param context
   * @param prefsname
   * @param defaultValue
   * @return
   */
  public static boolean getBoolean(Context context, String prefsname,
      boolean defaultValue) {
    try {
      return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
          prefsname, defaultValue);
    } catch (Exception ex) {
      try {
        // Try to convert it ourselves (in case it was stored as a
        // string)
        return Boolean.parseBoolean(PreferenceManager
            .getDefaultSharedPreferences(context).getString(prefsname,
                Boolean.toString(defaultValue)));
      } catch (Exception e) {
        Log.w(TAG, "Ignoring malformed preference: " + prefsname);

        return defaultValue;
      }
    }
  }
}
