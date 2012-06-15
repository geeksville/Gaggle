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
package com.geeksville.location;

import com.geeksville.gaggle.R;
import com.geeksville.location.baro.DummyBarometerClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

/// FIXME - add a basic vario http://www.paraglidingforum.com/viewtopic.php?p=48465
public class BarometerClient {

  @SuppressWarnings("unused")
  private static final String TAG = "BarometerClient";

  private static IBarometerClient instance = null;
  private static int instance_type;

  /**
   * All users of barometer share the same (expensive) instance
   * 
   * @return null for if not available
   */
  public static IBarometerClient create(Context context) {

    SensorClient.initManager(context);

	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

	final String vario_source = prefs.getString("vario_source", null);
	int vario_src;
	
	if (vario_source == null){
		return null;
	} else {
		vario_src = Integer.parseInt(vario_source);
	}
	
	/*
	 * if you've changed this pref after having created an instance
	 * then you'll have to stick to this instance until you restart.
	 * Else, we should transfer observers from one observable to the new one.
	 */
	if (instance == null){
		switch (vario_src){
		case 0:
			if (AndroidBarometerClient.isAvailable()){
				instance = new AndroidBarometerClient(context);
				instance_type = vario_src;
			}
			break;
		case 1: //CNES
			if (CNESBarometerClient.isAvailable()){
				instance = new CNESBarometerClient(context);
				instance_type = vario_src;
			}
			break;
		case 3:
			// FlyNet
			if (FlynetBarometerClient.isAvailable()){
				instance = new FlynetBarometerClient(context);
				instance_type = vario_src;
			}
			break;
		case 4:
			// Test BT
			break;
		case 5:
			instance = new DummyBarometerClient(context);
			instance_type = vario_src;
			break;
		}
	} else  if (instance != null && instance_type != vario_src){
		// trying to create the baro again from a different source
		CharSequence text = context.getString(R.string.baro_change_need_restart);
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
	}
	return instance;
  }
}
