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

		long prevTime = 0;
		Location p;
		try {
			while ((p = readLocation()) != null) {
				long newTime = p.getTime();
				l.add(p.getLatitude(), p.getLongitude(), (int) (p.getAltitude() / 1000.0),
						(int) (newTime - prevTime));
				prevTime = newTime;
			}
		} catch (Exception ex) {
			Log.w(TAG, "Malformed IGC file - ignoring error");
		}

		return l;
	}
}
