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
