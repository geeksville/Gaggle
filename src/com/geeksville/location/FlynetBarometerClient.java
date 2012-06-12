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
import android.hardware.SensorManager;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

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
  private static final String myName = "FlyNet";

  // Commands recognized by the FlyNet device
  private final String CMD_PRESSURE    = "_PRS";
  private final String CMD_BATTERY     = "_BAT";
  @SuppressWarnings("unused")
  private final String CMD_DEVICENAME  = "_USR";

  /** A unique ID for our app */
  private UUID uuid = UUID.fromString("b00d0c47-899b-4484-810a-5b47a516e906");

  private BluetoothDevice device;
  private Thread thread;

  private float batPercentage, pressure, altitude;
  private boolean isCharging = false;

  // / true if we've been set based on the GPS
  private boolean isCalibrated = false;

  // / Defaults to 1013.25 hPa
  private float reference = SensorManager.PRESSURE_STANDARD_ATMOSPHERE;
  LinearRegression regression = new LinearRegression();

  private Context context;

  public FlynetBarometerClient(Context context) {
    this.context = context;
    this.device = findDevice();

    // We do all the real work in a background thread, so we don't stall and can
    // handle reboots of the bluetooth device

    // FIXME this burns too much power, we should instead only create our reader
    // thread in addObserver, then
    // shut it down gracefully when the number of observers drops to zero. This
    // will have the nice effect of only talking
    // to the bluetooth baro when we actually need its data.
    thread = new Thread(this, "FlyNet");
    thread.setDaemon(true);
    thread.start();
    
    long xspan = (long) (PreferenceUtil.getFloat(context,
        "integration_period2", 0.7f) * 1000);
    regression.setXspan(xspan);
  }

  static boolean isAvailable() {
    BluetoothDevice found = findDevice();
    Log.d(TAG, "Found devices: " + found);
    return found != null;
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
    // float p0 = 1013.25f; // Pressure at sea level (hPa)
    // float p = p0 * (float) Math.pow((1 - meters / 44330), 5.255);
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

  @Override
  public float getVerticalSpeed() {
    return regression.getSlope() * 1000;
  }

  @Override
  public void improveLocation(Location l) {
    if (isCalibrated)
      l.setAltitude(altitude);
  }
  
  private void handleMessage(String m) {
    String cmd = m.substring(0,4);
    if (cmd.equals(CMD_PRESSURE)) {
      // "_PRS 17CBA\n" corresponds to 0x17CBA Pa
      pressure = Integer.parseInt(m.substring(6,10), 16) / 100.f;
      altitude = SensorManager.getAltitude(reference, pressure);
      regression.addSample(System.currentTimeMillis(), altitude);
    } else 
    if (cmd.equals(CMD_BATTERY)) {
      // "_BAT 9\n" corresponds to 90%
      // "_BAT *\n" signals charging status
      if (m.charAt(6) == '*') {
        isCharging = true;
      } else {
        batPercentage = (m.charAt(6) - '0') / 10.f;
        isCharging = false; // FIXME - may need a timeout if it actually alternates with the * message when charging
      }
    } 
    
    // TODO: calculate altitude and vertical speed if an initial altitude is given
      
    // Tell the GUI/audio vario we have new state
    setChanged();
    notifyObservers(pressure);
  }

  /** The background thread that talks to device */
  @Override
  public void run() {
    BluetoothSocket socket = null;
    try {
      Toast.makeText(context, "Using Bluetooth Vario", Toast.LENGTH_LONG)
          .show();

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
