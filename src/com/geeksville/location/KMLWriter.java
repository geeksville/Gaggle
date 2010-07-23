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

/*
 "
 <?xml version="1.0" encoding="UTF-8"?>
 <kml xmlns="http://www.opengis.net/kml/2.2">
 <Document>
 <name>KML Samples</name>
 <open>1</open>
 <description>Unleash your creativity with the help of these examples!</description>

 <Style id="yellowLineGreenPoly">
 <LineStyle>
 <color>7f00ffff</color>
 <width>4</width>
 </LineStyle>
 <PolyStyle>
 <color>7f00ff00</color>
 </PolyStyle>
 </Style>

 <Folder>
 <Placemark>
 <name>Absolute Extruded</name>
 <visibility>0</visibility>
 <description>Transparent green wall with yellow outlines</description>
 <LookAt>
 <longitude>-112.2643334742529</longitude>
 <latitude>36.08563154742419</latitude>
 <altitude>0</altitude>
 <heading>-125.7518698668815</heading>
 <tilt>44.61038665812578</tilt>
 <range>4451.842204068102</range>
 </LookAt>
 <styleUrl>#yellowLineGreenPoly</styleUrl>
 <LineString>
 <extrude>1</extrude>
 <tessellate>1</tessellate>
 <altitudeMode>absolute</altitudeMode>
 <coordinates> -112.2550785337791,36.07954952145647,2357
 -112.2549277039738,36.08117083492122,2357
 -112.2552505069063,36.08260761307279,2357
 -112.2564540158376,36.08395660588506,2357
 -112.2580238976449,36.08511401044813,2357
 -112.2595218489022,36.08584355239394,2357
 -112.2608216347552,36.08612634548589,2357
 -112.262073428656,36.08626019085147,2357
 -112.2633204928495,36.08621519860091,2357
 -112.2644963846444,36.08627897945274,2357
 -112.2656969554589,36.08649599090644,2357 </coordinates>
 </LineString>
 </Placemark>
 </Folder>
 </Document>
 </kml>      
 "
 */

public class KMLWriter implements PositionWriter {
	PrintStream out;
	boolean didProlog = false;

	String pilotName;
	String flightDesc;
	String gliderType;
	String pilotId;

	public KMLWriter(OutputStream dest, String pilotName, String flightDesc, String gliderType,
			String pilotId) throws IOException {
		out = new PrintStream(dest);

		this.gliderType = gliderType;
		this.pilotId = pilotId;
		this.pilotName = pilotName;
		this.flightDesc = flightDesc;
		// fixme - also write waypoints
	}

	/**
	 * We close the output stream in the epilog
	 */
	@Override
	public void emitEpilog() {

		out.println("</coordinates>" +
				"</LineString>" + "</Placemark>" +
				"</Document>" +
				"</kml>");

		out.close();
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
	public void emitPosition(long time, double latitude, double longitude, float altitude,
			int bearing, float groundSpeed) {
		// B
		// HHMMSS - time UTC
		// DDMMmmmN(or S) latitude
		// DDDMMmmmE(or W) longitude
		// A (3d valid) or V (2d only)
		// PPPPP pressure altitude (00697 in this case)
		// GGGGG alt above WGS ellipsode (00705 in this case)
		// GSP is 000 here (ground speed in km/hr)
		// B1851353728534N12151678WA0069700705000

		boolean is3D = !Double.isNaN(altitude);
		if (!is3D)
			altitude = 0f;

		// Use US format to ensure floats have dots not commas ;-)
		out.format(Locale.US, "%f,%f,%d\n", longitude, latitude, (int) altitude);
	}

	@Override
	public void emitProlog() {
		out
				.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+
				"<kml xmlns=\"http://www.opengis.net/kml/2.2\">"
				+
				"<Document>"
				+
				"<name>Gaggle KML</name>"
				+
				"<open>1</open>"
				+
				"<description>Gaggle KML file</description>"
				+
				"<Style id=\"yellowLineGreenPoly\">" +
				"<LineStyle>" +
				"<color>7f00ffff</color>" +
				"<width>4</width>" +
				"</LineStyle>" +
				"<PolyStyle>" +
				"<color>7f00ff00</color>" +
				"</PolyStyle>" +
				"</Style>" +
				"<Placemark>" +
				"<name>Flight</name>" +
				"<visibility>1</visibility>" +
				"<description>Gaggle Tracklog</description>" +
				"<styleUrl>#yellowLineGreenPoly</styleUrl>" +
				"<LineString>" +
				"<extrude>1</extrude>" +
				"<tessellate>1</tessellate>" +
				"<altitudeMode>absolute</altitudeMode>" +
				"<coordinates>");
	}
}
