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
    
    return instance;
  }

}
