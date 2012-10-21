/****************************************************************************************
 * Gaggle is Copyright 2010, 2011, and 2012 by Kevin Hester of Geeksville Industries LLC 
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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import com.geeksville.io.LineEndingStream;

/**
 * 
 * @author kevinh
 * 
 *         Writes IGC files (FAI gliding competition GPS data) Sample file
 *         follows:
 * 
 * 
 *         // name convention: 2009-12-25-XXX-SERN-YY.IGC // where XXX is the
 *         mfgr code. I'll pick GEK, YY is flight num for that day 01, etc... //
 *         if not paying fee I should use XXX
 * 
 *         // Contents // CR/LF at end of each line
 */
public class IGCWriter implements PositionWriter {

	private PrintStream out;
	private boolean didProlog = false;

	private String pilotName;
	private String flightDesc;
	private String gliderType;
	private String pilotId;
	private boolean hasJRecord = false;

	private Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

	public IGCWriter(OutputStream dest, String pilotName, String flightDesc,
			String gliderType, String pilotId) throws IOException {
		out = new PrintStream(new LineEndingStream(dest));

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
		// sect 3.2, G=security record
		// Generate fake G records
		out.println("GGaggleDoesntDoGRecordsYet");

		// out.println("G03C15AF3BC8A4A288FAB70442A567DEB");

		out.close();
	}

	/**
	 * Return a degress in IGC format
	 * 
	 * @param degIn
	 * @return
	 */
	private static String degreeStr(double degIn, boolean isLatitude) {
		boolean isPos = degIn >= 0;
		char dirLetter = isLatitude ? (isPos ? 'N' : 'S') : (isPos ? 'E' : 'W');

		degIn = Math.abs(degIn);
		double minutes = 60 * (degIn - Math.floor(degIn));
		degIn = Math.floor(degIn);
		int minwhole = (int) minutes;
		int minfract = (int) ((minutes - minwhole) * 1000);

		// DDMMmmmN(or S) latitude
		// DDDMMmmmE(or W) longitude
		String s = String.format(Locale.US, (isLatitude ? "%02d" : "%03d")
				+ "%02d%03d%c", (int) degIn, minwhole, minfract, dirLetter);
		return s;
	}

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
	public void emitPosition(long time, double latitude, double longitude,
			float altitude, int bearing, float groundSpeed, float[] accel,
			float vspd) {
		// B
		// HHMMSS - time UTC
		// DDMMmmmN(or S) latitude
		// DDDMMmmmE(or W) longitude
		// A (3d valid) or V (2d only)
		// PPPPP pressure altitude (00697 in this case)
		// GGGGG alt above WGS ellipsode (00705 in this case)
		// GSP is 000 here (ground speed in km/hr)
		// B1851353728534N12151678WA0069700705000

		// Get time in UTC
		cal.setTimeInMillis(time);

		boolean is3D = !Double.isNaN(altitude);

		// Spit out our prolog if need be
		if (!didProlog) {
			emitProlog(cal);
			didProlog = true;
		}

		int hours = cal.get(Calendar.HOUR_OF_DAY);
		out.format(Locale.US, "B%02d%02d%02d%s%s%c%05d%05d%03d", hours, cal
				.get(Calendar.MINUTE), cal.get(Calendar.SECOND),
				degreeStr(latitude, true), degreeStr(longitude, false),
				is3D ? 'A' : 'V', (int) (is3D ? altitude : 0), // FIXME convert
																// altitudes
				// correctly
				(int) (is3D ? altitude : 0), // FIXME convert alts
				(int) groundSpeed);
		out.println();

		// Don't store vertical speed info until I can find an example data
		// file.
		if (!Float.isNaN(vspd) && false) {

			if (!hasJRecord) {
				// less frequent extension - vario data
				out.println("J010812VAR");
				hasJRecord = true;
			}

			out.format(Locale.US, "K%02d%02d%02d%03d", hours,
					cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND),
					(int) vspd * 10);
			out.println();
		}
	}

	/**
	 * Do the heavy lifting necessary to spit out a file header
	 */
	private void emitProlog(Calendar cal) {

		out.println("AXXXGaggle"); // AFLY06122 - sect 3.1, A=mfgr info,
		// mfgr=FLY, serial num=06122

		// sect 3.3.1, H=file header
		String dstr = String.format(Locale.US, "HFDTE%02d%02d%02d",
				cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1,
				(cal.get(Calendar.YEAR) - 1900) % 100); // date

		out.println(dstr); // date

		out.println("HFFXA100"); // accuracy in meters - required
		out.println("HFPLTPILOT:" + pilotName); // pilot (required)
		out.println("HFGTYGLIDERTYPE:" + gliderType); // glider type (required)
		out.println("HFGIDGLIDERID:" + pilotId); // glider ID required
		out.println("HFDTM100GPSDATUM:WGS84"); // datum required - must be wgs84
		out.println("HFGPSGPS:" + android.os.Build.MODEL); // info on gps
		// manufactuer
		out.println("HFRFWFIRMWAREVERSION:0.10"); // sw version of app
		out.println("HFRHWHARDWAREVERSION:1.00"); // hw version
		out.println("HFFTYFRTYPE:Geeksville,Gaggle"); // required: manufacture
		// (me) and model num

		// sect 3.4, I=fix extension list
		out.println("I013638GSP"); // one extension, starts at byte 36, ends at
		// 38, extension type is ground speed (was TAS)
	}

	/**
	 * Add standard IGC prologue
	 * 
	 */
	@Override
	public void emitProlog() {
	}

}
