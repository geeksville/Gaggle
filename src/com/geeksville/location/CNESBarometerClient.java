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
import java.util.Observable;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

/**
 * A client for scott@cnes.com bluetooth vario
 * 
 * @author kevinh
 * 
 *         On Mon, Feb 6, 2012 at 11:47 AM, Scott Jepson <scott@cnes.com> wrote:
 * 
 *         Hi Kevin, So here's the output from the bluetooth barometer:
 *         printf("V%06ld A%07ld P%06ld B%03ld\r\n",Vspd,Alt,Pressure,**Battery)
 * 
 *         Vspd=CM/S and looks like 000005 or -00005 Alt=CM and looks like
 *         0038002 which would be 380.02 Meters Pressure=Pa and looks like
 *         096840 Battery=Volts and looks like 032 which would be 3.2 volts
 * 
 *         So the whole output looks like: V000005 A0038002 P096840 B032
 * 
 *         How fast do you want the data samples sent? So far I can do 25
 *         samples a second.
 * 
 * 
 *         -Scott
 */
public class CNESBarometerClient extends Observable implements
    IBarometerClient, Runnable {

  private static final String TAG = "BluetoothBarometerClient";

  // / What device name do we look for?
  private static final String myName = "BlueBaro";

  // private static final int myClass = 0xa01; // See
  // http://developer.android.com/reference/android/bluetooth/BluetoothClass.Device.html,
  // for now I'm guessing at a
  // value

  /** A unique ID for our app */
  private UUID uuid = UUID.fromString("b00d0c47-899b-4484-810a-5b27a514e906");

  private BluetoothDevice device;
  private Thread thread;

  private float altitude, vspd, batVoltage, pressure;

  // / true if we've been set based on the GPS
  private boolean isCalibrated = false;

  public CNESBarometerClient(Context context) {
    this.device = findDevice();

    // We do all the real work in a background thread, so we don't stall and can
    // handle reboots of the bluetooth device

    // FIXME this burns too much power, we should instead only create our reader
    // thread in addObserver, then
    // shut it down gracefully when the number of observers drops to zero. This
    // will have the nice effect of only talking
    // to the bluetooth baro when we actually need its data.
    thread = new Thread(this, "BluetoothBaro");
    thread.setDaemon(true);
    thread.start();
  }

  static boolean isAvailable() {
    return findDevice() != null;
  }
  
  public String getStatus() {
    return "CNES";
  }

  private static BluetoothDevice findDevice() {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    if (adapter != null && adapter.isEnabled()) {
      Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

      for (BluetoothDevice device : pairedDevices) {

        Log.d(TAG,
            "Considering " + device.getName() + "@" + device.getAddress());

        // if (device.getBluetoothClass().getDeviceClass() == myClass) return
        // device;
        if (device.getName().startsWith(myName))
          return device;
      }
    }

    return null;
  }

  @Override
  public void setAltitude(float meters) {
    // FIXME - apply correction from GPS based altitude
  }

  public float getPressure() {
    return pressure;
  }

  @Override
  public float getAltitude() {
    return altitude;
  }

  @Override
  public float getVerticalSpeed() {
    return vspd;
  }

  public float getBattery() {
    return batVoltage;
  }

  public float getBatteryPercent() {
    return Float.NaN; //FIXME
  }

  @Override
  public void improveLocation(Location l) {
    if (isCalibrated)
      l.setAltitude(altitude);
  }

  private void handleMessage(String m) {
    /**
     * Hi Kevin, So here's the output from the bluetooth barometer:
     * printf("V%06ld A%07ld P%06ld B%03ld\r\n",Vspd,Alt,Pressure,**Battery)
     * 
     * Vspd=CM/S and looks like 000005 or -00005 Alt=CM and looks like 0038002
     * which would be 380.02 Meters Pressure=Pa and looks like 096840
     * Battery=Volts and looks like 032 which would be 3.2 volts
     * 
     * So the whole output looks like: V000005 A0038002 P096840 B032
     */
    vspd = Integer.parseInt(m.substring(1, 1 + 6)) / 100.0f; // avoid using
                                                             // split,
    // because it generates
    // lots of allocs
    altitude = Integer.parseInt(m.substring(9, 9 + 7)) / 100.f;

    // convert pressure from Pa to hPa
    pressure = Integer.parseInt(m.substring(18, 18 + 6)) / 100.f;

    batVoltage = Integer.parseInt(m.substring(26, 26 + 3)) / 10.0f;

    // Tell the GUI/audio vario we have new state
    setChanged();
    notifyObservers(pressure);
  }

  /** The background thread that talks to device */
  @Override
  public void run() {
    BluetoothSocket socket = null;
    try {
      // FIXME, add outer loop to reconnect if bluetooth device is rebooted
      socket = device.createRfcommSocketToServiceRecord(uuid);

      // Connect the device through the socket. This will block
      // until it succeeds or throws an exception
      socket.connect();

      // Read messages
      BufferedReader reader = new BufferedReader(new InputStreamReader(
          socket.getInputStream()));

      String line;
      while ((line = reader.readLine()) != null)
        handleMessage(line);

      reader.close();

      socket.close();
      socket = null;
    } catch (IOException connectException) {
      // close the socket and get out
      try {
        if (socket != null)
          socket.close();
      } catch (IOException closeException) {
        // Ignore errors on close
      }
    }
  }

}
