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

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * A little class that automates connecting to the GPSClient service
 * 
 * @author kevinh
 * 
 */
public class GPSClientStub implements ServiceConnection {

	IGPSClient gps;
	Context context;

	public GPSClientStub(Context context) {
		this.context = context;

		GPSClient.bindTo(context, this);
	}

	/**
	 * You must call this to avoid leaks
	 */
	public void close() {
		GPSClient.unbindFrom(context, this);
	}

	public IGPSClient get() {
		return gps;
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		gps = (IGPSClient) service;
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		gps = null;
	}
}
