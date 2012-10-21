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

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.widget.Toast;

/**
 * Reads an IGC file to move the Android GPS location around (for development)
 * 
 * @author kevinh
 * 
 */
public class TestGPSDriver {

  /**
   * Debugging tag
   */
  @SuppressWarnings("unused")
  private static final String TAG = "TestGPS";

  private LocationManager manager;

  private String provider = "simdata";

  private GPSDriverThread myThread = new GPSDriverThread();

  private IGCReader inData;

  private boolean isExiting = false;
  private Context context;

  public TestGPSDriver(Context context) {
    this.context = context;

    manager = (LocationManager) context
        .getSystemService(Context.LOCATION_SERVICE);

    // Blow away any old providers
    try {
      manager.removeTestProvider(provider);
    } catch (Exception ex) {
      // Ignore
    }

    manager.addTestProvider(provider, false, false, false, false, true, true,
        true, Criteria.POWER_LOW, Criteria.ACCURACY_FINE);

    // Turn the GPS on and claim tracking
    manager.setTestProviderEnabled(provider, true);

    try {
      InputStream s = context.getAssets().open("testfile.igc");
      inData = new IGCReader(provider, s);
    } catch (IOException ex) {
      // We should never fail opening this file
      throw new RuntimeException(ex);
    }

    myThread.start();
  }

  public String getProvider() {
    return provider;
  }

  /**
   * Shut down our sim data
   */
  public void close() {

    isExiting = true; // Let our thread notice this the next time he wakes
    // up
  }

  private void sendNewUpdate() {
    // Bundle extras = new Bundle();
    // long updateTime = 0;
    // manager.setTestProviderStatus(provider,
    // GpsStatus.GPS_EVENT_SATELLITE_STATUS, extras, updateTime);

    Location loc = null;
    try {
      loc = inData.readLocation();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    try {
      if (loc != null) {
        loc.setProvider(provider);
        manager.setTestProviderLocation(provider, loc);
      }
    } catch (SecurityException ex) {
      close();
      Toast.makeText(context, "Simulated GPS data disabled by your device",
          Toast.LENGTH_LONG).show();
    }
  }

  private class GPSDriverThread extends Thread {

    /**
     * Constructor
     */
    public GPSDriverThread() {
      super("SimGPS");
    }

    /*
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
      try {
        Thread.sleep(3 * 1000);
        // Pretend to find sats
        /*
         * Bundle extras = new Bundle(); manager.setTestProviderStatus(provider,
         * GpsStatus.GPS_EVENT_STARTED, extras, (new Date()).getTime());
         * manager.setTestProviderStatus(provider,
         * GpsStatus.GPS_EVENT_FIRST_FIX, extras, (new Date()).getTime());
         * manager.setTestProviderStatus(provider,
         * GpsStatus.GPS_EVENT_SATELLITE_STATUS, extras, (new
         * Date()).getTime());
         */

        while (!isExiting) {
          Thread.sleep(500 /* 3 * 1000 */);
          sendNewUpdate();
        }

      } catch (InterruptedException ex) {
        // Just exit
      }

      try {
        inData.close();
      } catch (IOException ex) {
        // Ignore errors on close
      }

      manager.clearTestProviderEnabled(provider);
      manager.clearTestProviderLocation(provider);
      manager.clearTestProviderStatus(provider);

      manager.removeTestProvider(provider);
    }

  }
}
