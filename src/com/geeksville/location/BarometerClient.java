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

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

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

    // Prefer to use external bluetooth device
    if (instance == null && FlynetBarometerClient.isAvailable())
      instance = new FlynetBarometerClient(context);
    if (instance == null && CNESBarometerClient.isAvailable())
      instance = new CNESBarometerClient(context);
    if (instance == null && AndroidBarometerClient.isAvailable())
      instance = new AndroidBarometerClient(context);
    
    /*if (instance != null)
      Toast.makeText(context, "Discovered: "+instance.getStatus(), 1000);
    else
      Toast.makeText(context, "No barometer available.", 1000);*/

    return instance;
  }

}
