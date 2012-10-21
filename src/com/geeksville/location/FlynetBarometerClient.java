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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.hardware.SensorManager;
import android.location.Location;
import android.util.Log;

import com.geeksville.android.PreferenceUtil;
import com.geeksville.util.LinearRegression;

/**
 * A client for the FlyNet bluetooth vario
 * 
 * @author relet
 */

public class FlynetBarometerClient extends Observable implements
    IBarometerClient, Runnable {

  private static final String TAG = "FlynetBarometerClient";

  // / What device name do we look for?
  // private static final String myName = "FlyNet";
  // / What device type
  private static final int myClass = 7936;

  // Commands recognized by the FlyNet device
  private final String CMD_PRESSURE    = "_PRS";
  private final String CMD_BATTERY     = "_BAT";
  @SuppressWarnings("unused")
  private final String CMD_DEVICENAME  = "_USR";

  /** A unique ID for our app */
  @SuppressWarnings("unused")
  private UUID uuid = UUID.fromString("b00d0c47-899b-4484-810a-5b27a514e906");

  private BluetoothDevice device;
  private Thread thread;
  private String status = "FlyNet";

  private float batPercentage, pressure, altitude;
  private boolean isCharging = false;

  // / true if we've been set based on the GPS
  private boolean isCalibrated = false;

  // / Defaults to 1013.25 hPa
  private float reference = SensorManager.PRESSURE_STANDARD_ATMOSPHERE;
  LinearRegression regression = new LinearRegression();

  public FlynetBarometerClient(Context context) {
    this.device = findDevice();

    long xspan = (long) (PreferenceUtil.getFloat(context,
        "integration_period2", 0.7f) * 1000);
    regression.setXspan(xspan);
  }
  
  public void addObserver(Observer observer) {
    super.addObserver(observer);

    // We do all the real work in a background thread, so we don't stall and can
    // handle reboots of the bluetooth device
    if ((thread==null)||(thread.isAlive()==false)) {
      thread = new Thread(this, "FlyNet");
      thread.setDaemon(true);
      thread.start();
    }
  }

  static boolean isAvailable() {
    BluetoothDevice found = findDevice();
    return found != null;
  }

  /**
   * Check device exists and is enabled.
   * 
   * - Is there a message elsewhere to say "Have you turned the device on?"
   * - Is the option shown disabled or does it only appear if the device is found?
   * 
   * @return
   */
  private static BluetoothDevice findDevice() {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    if (adapter != null && adapter.isEnabled()) {
      Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

      for (BluetoothDevice device : pairedDevices) {
        if ((device.getBluetoothClass().getDeviceClass() == myClass)) {
          Log.d(TAG,
              "Connected to " + device.getName() + "@" + device.getAddress() + " which has device ID " + device.getBluetoothClass().getDeviceClass());
          return device;
        }
      }
    }

    return null;
  }

  /**
   * Set the altitudes in meters above sea level
   */
  @Override
  public void setAltitude(float meters) {
    float p0 = pressure / (float) Math.pow((1 - meters / 44330), 5.255);

    reference = p0;
    altitude = SensorManager.getAltitude(reference, pressure);

    Log.w(TAG, "Setting baro reference to " + reference + " alt=" + meters);
    isCalibrated = true;
  }

  @Override
  public float getAltitude() {
    return altitude;
  }

  public float getPressure() {
    return pressure;
  }
  public float getBattery() {
    return Float.NaN;
    // FIXME - if we know the battery size, we can calculate this from the percentage.
  }
  public float getBatteryPercent() {
    return batPercentage;
  }
  public boolean isCharging() {
    return isCharging;
  }
  public String getStatus() {
    return status;
  }
  public void setStatus(String s) {
    status = s;
  }

  @Override
  public float getVerticalSpeed() {
    try {
      return regression.getSlope() * 1000;
    } catch (ArithmeticException divByZero) {
      return Float.NaN;
    }
  }

  @Override
  public void improveLocation(Location l) {
    if (isCalibrated)
      l.setAltitude(altitude);
  }
  
  private void handleMessage(String m) {
    if (m.length()>4) {
      String cmd = m.substring(0,4);
      if (cmd.equals(CMD_PRESSURE)) {
        // "_PRS 17CBA\n" corresponds to 0x17CBA Pa
        pressure = Integer.parseInt(m.substring(5,10), 16) / 100.f;
        altitude = SensorManager.getAltitude(reference, pressure);
        regression.addSample(System.currentTimeMillis(), altitude);
        //Log.d(TAG, "-> pressure = " + pressure + "\t altitude = "+altitude);
      } else 
      if (cmd.equals(CMD_BATTERY)) {
        // "_BAT 9\n" corresponds to 90%
        // "_BAT *\n" signals charging status
        if (m.charAt(5) == '*') {
          isCharging = true;
        } else {
          batPercentage = Integer.parseInt(m.substring(5,6), 16) / 16.f;
          isCharging = false; // FIXME - may need a timeout if it actually alternates with the * message when charging
        }
      } 
      
      // Tell the GUI/audio vario we have new state
      setChanged();
      notifyObservers(pressure);
    }
  }

  /** The background thread that talks to device */
  @Override
  public void run() {
    BluetoothSocket socket = null;
      
    do {
      setStatus("? FlyNet");
      try {
        //socket = device.createRfcommSocketToServiceRecord(uuid); /* NOTE - does not work in Android 2.1/2.2 */
        BluetoothDevice hxm = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(device.getAddress());
        Method m;
        try {
          m = hxm.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
          socket = (BluetoothSocket)m.invoke(hxm, Integer.valueOf(1));
        } catch (Exception e) {
          Log.d(TAG, "Error while creating socket", e);
        }
      
        if (socket != null) {
        // Connect the device through the socket. This will block
        // until it succeeds or throws an exception
          socket.connect();
    
          // Read messages
          BufferedReader reader = new BufferedReader(new InputStreamReader(
              socket.getInputStream()));
    
          String line;
          setStatus("+ FlyNet");
          while ((line = reader.readLine()) != null)
            handleMessage(line);
    
          reader.close();
    
          setStatus("- FlyNet");
    
          socket.close();
          socket = null;
        }
      } catch (IOException connectException) {
        // close the socket and get out
        Log.d(TAG, "Error while connecting", connectException);
        try {
          if (socket != null)
            socket.close();
        } catch (IOException closeException) {
          // Ignore errors on close
        }
      }
    } while (this.countObservers() > 0);
  }

}
