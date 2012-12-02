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
		out.format(Locale.US, "%d,%f,%f,%f,%d,%f,%f", time, latitude, longitude, altitude, bearing, groundSpeed, vspd);

		if (accel == null)
			accel = fakeAccel;

		out.format(Locale.US, ",%f,%f,%f", accel[0], accel[1], accel[2]);

		for (int i = 0; i < NUM_FAKE; i++)
			out.format(",%f", 0.80 + 0.20 * Math.random());

		out.println();
	}

	@Override
	public void emitProlog() {
		out.print("mSec,Latitude,Longitude,Altitude,Bearing,Speed,VertSpeed");
		out.print(",AccelX,AccelY,AccelZ");
		out.print(",Soc1,Soc2,Soc3,BDat1,BDat2,BDat3");
		out.println();
	}
}
