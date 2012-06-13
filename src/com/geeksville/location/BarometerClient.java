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

/// FIXME - add a basic vario http://www.paraglidingforum.com/viewtopic.php?p=48465
public class BarometerClient {

  @SuppressWarnings("unused")
  private static final String TAG = "BarometerClient";

  private static IBarometerClient instance = null;

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
	
	if (instance == null){
		switch (vario_src){
		case 0:
			if (AndroidBarometerClient.isAvailable())
				instance = new AndroidBarometerClient(context);
			break;
		case 1: //CNES
			if (CNESBarometerClient.isAvailable())
				instance = new CNESBarometerClient(context);
			break;
		case 3:
			// FlyNet
			if (FlynetBarometerClient.isAvailable())
				instance = new FlynetBarometerClient(context);
			break;
		case 4:
			// Test BT
			break;
		case 5:
			instance = new DummyBarometerClient(context);
			break;
		}
	}
	return instance;
  }
}
