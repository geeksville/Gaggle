/****************************************************************************************
 * Gaggle is Copyright 2010, 2011, and 2012 by Kevin Hester of Geeksville Industries LLC 
 * a California limited liability corporation and sly sylvain at letuffe d org 27/04/2011;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/*
 * GPX file generator from gaggle tracklogs
 * created by sly sylvain at letuffe d org 27/04/2011
 */
/*
gpx file, simple example
 "<?xml version="1.0" encoding="UTF-8"?>
<gpx version="1.0" creator="Gaggle https://github.com/geeksville/Gaggle" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://www.topografix.com/GPX/1/0" xsi:schemaLocation="http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd">
<trk>
  <name>Flight's name</name>
  <desc>what ever</desc>	
<trkseg>
<trkpt lat="45.563400000" lon="5.916866667">
  <ele>342.000000</ele>
<time>2011-04-20T18:31:10Z</time>
</trkpt>
<trkpt lat="45.563466667" lon="5.916783333">
  <ele>362.000000</ele>
<time>2011-04-20T18:31:16Z</time>
</trkpt>
</trkseg>
</trk>
</gpx>
 "
 */

public class GPXWriter implements PositionWriter {
	PrintStream out;
	boolean didProlog = false;

	String pilotName;
	String flightDesc;
	String gliderType;
	String pilotId;

	float minHeight = Float.MAX_VALUE, maxHeight = Float.MIN_VALUE;
	long firstTimeMs, lastTimeMs;
	boolean isFirst = true;

	public GPXWriter(OutputStream dest, String pilotName, String flightDesc, String gliderType,
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

		out.println("\t</trkseg>\n"+
					"</trk>\n" +
					"</gpx>\n");
		out.close();
	}

	/**
	 * 
	 * @param time
	 *            UTC time of this fix, in milliseconds since January 1, 1970.
	 * @param latitude
	 * @param longitude
	 * 
	 */
	@Override
	public void emitPosition(long time, double latitude, double longitude, float altitude,
			int bearing, float groundSpeed, float[] accel, float vspd) {
		/* Trackpoint gpx format :			
 		<trkpt lat="45.563466667" lon="5.916783333">
		  <ele>362.000000</ele>
		<time>2011-04-20T18:31:16Z</time>
		</trkpt>
		*/

		boolean is3D = !Double.isNaN(altitude);
		if (!is3D)
			altitude = 0;

		out.format(Locale.US,"\t\t<trkpt lat=\"%f\" lon=\"%f\">\n",latitude,longitude);
		out.format(Locale.US,"\t\t\t<ele>%f</ele>\n",altitude);
		out.printf("\t\t<time>%s</time>\n",GetGPXTimeStamp(time));
		out.println("\t\t</trkpt>\n");
	}
/*
 * Generate a ISO 8601 UTC formated date as used in gpx format
 */
	private String GetGPXTimeStamp(long numMs) {
		Date d = new Date(numMs);
		
		final SimpleDateFormat ISO8601UTC = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
		ISO8601UTC.setTimeZone(TimeZone.getTimeZone("UTC")); 
		String FormatedDateISO8601 = ISO8601UTC.format(d);
		
		return FormatedDateISO8601;
	}

	/* Simple gpx header with just pilot's name, id and glider type*/
	@Override
	public void emitProlog() {
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
					"<gpx version=\"1.0\" creator=\"Gaggle https://github.com/geeksville/Gaggle\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.topografix.com/GPX/1/0\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">\n" +
					"<trk>\n" +
					"\t<name>" + pilotName + "'s flight</name>\n" +
					"\t<desc>\n"+
					String.format("\tPilot name:\t%s\n", pilotName) +
					String.format("\tPilot ID:\t%s\n", pilotId) +
					String.format("\tGlider type:\t%s\n", gliderType) +
					"\t</desc>\n" +	
					"\t<trkseg>");
	}
}
