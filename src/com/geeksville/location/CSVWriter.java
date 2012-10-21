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
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

public class CSVWriter implements PositionWriter {
	PrintStream out;
	boolean didProlog = false;

	String pilotName;
	String flightDesc;
	String gliderType;
	String pilotId;

	public CSVWriter(OutputStream dest, String pilotName, String flightDesc, String gliderType,
			String pilotId) throws IOException {
		out = new PrintStream(dest);

		this.gliderType = gliderType;
		this.pilotId = pilotId;
		this.pilotName = pilotName;
		this.flightDesc = flightDesc;
	}

	/**
	 * We close the output stream in the epilog
	 */
	@Override
	public void emitEpilog() {

		out.close();
	}

	private static final int NUM_FAKE = 6;

	private float[] fakeAccel = new float[3];

	/**
	 * 
	 * @param time
	 *            UTC time of this fix, in milliseconds since January 1, 1970.
	 * @param latitude
	 * @param longitude
	 * 
	 *            sect 4.1, B=fix plus extension data mentioned in I
	 */
	@Override
	public void emitPosition(long time, double latitude, double longitude, float altitude,
			int bearing, float groundSpeed, float[] accel, float vspd) {

		// Use US format to ensure floats have dots not commas ;-)
		out.format(Locale.US, "%d,%f,%f,%f,%f", time, latitude, longitude, altitude, groundSpeed);

		if (accel == null)
			accel = fakeAccel;

		out.format(Locale.US, ",%f,%f,%f", accel[0], accel[1], accel[2]);

		for (int i = 0; i < NUM_FAKE; i++)
			out.format(",%f", 0.80 + 0.20 * Math.random());

		out.println();
	}

	@Override
	public void emitProlog() {
		out.print("mSec,Latitude,Longitude,Altitude,Speed");
		out.print(",AccelX,AccelY,AccelZ");
		out.print(",Soc1,Soc2,Soc3,BDat1,BDat2,BDat3");
		out.println();
	}
}
