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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.location.Location;
import android.util.Log;

/**
 * Reads IGC files
 * 
 * @author kevinh
 * 
 */
public class IGCReader {

	BufferedReader inData;

	SimpleDateFormat formater = new SimpleDateFormat("HHmmss");

	private static final String TAG = "IGCReader";

	/**
	 * populated in our location objects
	 */
	String provider;

	public IGCReader(String provider, InputStream stream) {
		inData = new BufferedReader(new InputStreamReader(stream));
	}

	public void close() throws IOException {
		inData.close();
	}

	/**
	 * read the next location from the file (or null for eof)
	 * 
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public Location readLocation() throws ParseException, IOException {
		Location res = null;

		do {
			String line = inData.readLine();
			if (line == null)
				return null; // eof

			if (line.startsWith("B")) {
				// This is a position record
				// B
				// HHMMSS - time UTC
				// DDMMmmmN(or S) latitude
				// DDDMMmmmE(or W) longitude
				// A (3d valid) or V (2d only)
				// PPPPP pressure altitude (00697 in this case)
				// GGGGG alt above WGS ellipsode (00705 in this case)
				// GSP is 000 here (ground speed in km/hr)
				// B2109233921018N12239641WA0051600526000
				// int hr = Integer.parseInt(line.substring(1, 3));
				// int min = Integer.parseInt(line.substring(3, 5));
				// int sec = Integer.parseInt(line.substring(5, 7));

				String timestr = line.substring(1, 7);

				int latdeg = Integer.parseInt(line.substring(7, 9));
				int latmin = Integer.parseInt(line.substring(9, 11));
				double latminfract = Integer.parseInt(line.substring(11, 14));
				char latdir = line.charAt(14);

				int longdeg = Integer.parseInt(line.substring(15, 18));
				int longmin = Integer.parseInt(line.substring(18, 20));
				double longminfract = Integer.parseInt(line.substring(20, 23));
				char longdir = line.charAt(23);

				int alt = Integer.parseInt(line.substring(25, 25 + 5));

				double lat = (latdeg + (latmin + latminfract / 1000) / 60.0)
							* (latdir == 'N' ? 1 : -1);

				double ltude = (longdeg + (longmin + longminfract / 1000) / 60.0)
							* (longdir == 'E' ? 1 : -1);

				// Date d = new Date(); // Claim the flight is happening now
				Date d = formater.parse(timestr);

				// FIXME - we should also pay attention to the TZ and the date
				// stored in the file header

				res = new Location(provider);
				res.setAltitude(alt);
				res.setTime(d.getTime());
				res.setLatitude(lat);
				res.setLongitude(ltude);

				// Log.d(TAG, "SimPos: " + lat + "," + ltude + "," + alt);
			}
		} while (res == null);

		return res;
	}

	/**
	 * Return an in memory copy of just the points in this IGC file
	 * 
	 * @return
	 */
	public LocationList toLocationList() {
		LocationList l = new LocationList();

		long firstTime = -1;
		Location p;
		try {
			while ((p = readLocation()) != null) {
				long newTime = p.getTime();

				if (firstTime == -1)
					firstTime = newTime;

				l.add(p.getLatitude(), p.getLongitude(), (int) (p.getAltitude() * 1000.0),
						(int) (newTime - firstTime));
			}
		} catch (Exception ex) {
			Log.w(TAG, "Malformed IGC file - ignoring error");
		}

		return l;
	}
}
