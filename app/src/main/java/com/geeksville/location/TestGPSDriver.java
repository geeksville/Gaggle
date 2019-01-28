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

  private Location readLocation() {
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
    
    return loc;
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
    	long timeRecordBase = 0;
    	long timeElapsed = 0;
    	
    	try
    	{
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
    		Location loc = readLocation();
    		if(loc != null)
    		{
    			timeRecordBase = loc.getTime();
    		}
    		
    		while(!isExiting)
    		{
	   			while(loc != null && loc.getTime() - timeRecordBase <= timeElapsed)
	   			{
	   			    try
	   			    {
	   			        loc.setProvider(provider);
	   			        manager.setTestProviderLocation(provider, loc);
	   			    } catch (SecurityException ex)
	   			    {
	   			      close();
	   			      Toast.makeText(context, "Simulated GPS data disabled by your device",
	   			          Toast.LENGTH_LONG).show();
	   			    }

	   				loc = readLocation();
	   			}
	   			
	   			Thread.sleep(500);
	   			timeElapsed += 500;
    		}
   		} catch (InterruptedException ex)
   		{
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
