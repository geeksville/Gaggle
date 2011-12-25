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

import java.util.Date;

import android.content.Context;

/**
 * A position writer that emits to a simple DB of flights
 * 
 * @author kevinh
 * 
 */
public class LocationDBWriter implements PositionWriter {

	private LocationLogDbAdapter db;

	/**
	 * Have we started writing to the DB yet?
	 */
	private boolean flightStarted = false;

	private String pilotName;

	private String flightDesc;

	/**
	 * True if we should upload on completion
	 */
	private boolean wantUpload;

	/**
	 * The num msec since 1970 for the most recent point
	 */
	private long lastTime;

	/**
	 * The db key for this flt
	 */
	private long flightId;

	/**
	 * Constructor
	 * 
	 * @param context
	 * @param wantUpload
	 *            true if we'd like this flight uploaded asap
	 */
	public LocationDBWriter(Context context, boolean wantUpload,
			String pilotName, String flightDesc) {
		db = new LocationLogDbAdapter(context);

		this.wantUpload = wantUpload;
		this.pilotName = pilotName;
		this.flightDesc = flightDesc;
	}

	@Override
	public void emitEpilog() {
		db.updateFlight(flightId, new Date(lastTime), null, wantUpload, false);

		db.close();
	}

	@Override
	public void emitPosition(long time, double latitude, double longitude,
			float altitude, int bearing, float groundSpeed, float[] accel,
			float vspd) {

		lastTime = time;

		if (!flightStarted) {
			flightStarted = true;

			flightId = db.createFlight(pilotName, flightDesc, time);
		}

		// FIXME, figure out if reported bearing is a heading or a ground track
		db.addLocation(flightId, time, latitude, longitude, altitude, bearing,
				groundSpeed, accel, vspd);
	}

	@Override
	public void emitProlog() {
	}

}
