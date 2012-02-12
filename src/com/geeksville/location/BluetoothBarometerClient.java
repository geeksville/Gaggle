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
import android.location.Location;
import android.util.Log;

/**
 * A client for scott@cnes.com bluetooth vario
 * 
 * @author kevinh
 * 
 */
public class BluetoothBarometerClient extends Observable implements
		IBarometerClient, Runnable {

	private static final String TAG = "BluetoothBarometerClient";

	private static final int myClass = 0xa01; // See
												// http://developer.android.com/reference/android/bluetooth/BluetoothClass.Device.html,
												// for now I'm guessing at a
												// value

	/** A unique ID for our app */
	private UUID uuid = UUID.fromString("b00d0c47-899b-4484-810a-5b27a514e906");

	private BluetoothSocket socket;

	private BluetoothBarometerClient(BluetoothDevice device) {
		// Get a BluetoothSocket to connect with the given BluetoothDevice
		try {
			// MY_UUID is the app's UUID string, also used by the server code
			socket = device.createRfcommSocketToServiceRecord(uuid);
		} catch (IOException e) {
			// FIXME
		}
	}

	static boolean isAvailable() {
		return findDevice() != null;
	}

	private static BluetoothDevice findDevice() {
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		if (adapter != null) {
			Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

			for (BluetoothDevice device : pairedDevices) {

				Log.d(TAG,
						"Considering " + device.getName() + "@"
								+ device.getAddress());

				int bClass = device.getBluetoothClass().getDeviceClass();

				if (bClass == myClass)
					return device;
			}
		}

		return null;
	}

	@Override
	public void setAltitude(float meters) {
		// TODO Auto-generated method stub

	}

	@Override
	public float getAltitude() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getVerticalSpeed() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void improveLocation(Location l) {
		// TODO Auto-generated method stub

	}

	// / The background thread that talks to device
	@Override
	public void run() {
		try {
			// Connect the device through the socket. This will block
			// until it succeeds or throws an exception
			socket.connect();

			// Read messages
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
		} catch (IOException connectException) {
			// Unable to connect; close the socket and get out
			try {
				socket.close();
			} catch (IOException closeException) {
				// Ignore errors on close
			}
		}
	}

}
