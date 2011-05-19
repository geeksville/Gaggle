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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Import waypoints (.wpt files into our DB)
 * 
 * @author kevinh
 * 
 *         Format 1: We expect the file to be of the following form: AIRSTR N 36
 *         42 59.34 W 119 08 15.54 641 AIRSTRIP
 * 
 *         Format 2: a totally unrelated format (compegps)
 * 
 *         G European 1979 U 1 W T01068 A 46.1114209361ºN 13.7287505037ºE
 *         27-MAR-62 00:00:00 680.000000 T01068 T01 w
 *         Waypoint,0,-1.0,16777215,255,1,7,,0.0 W T02017 A 46.1233699909ºN
 *         13.8034655770ºE 27-MAR-62 00:00:00 175.000000 T02017 T02
 * 
 *         For this format the only row that matters is: W T04220 A
 *         46.3678156439ºN 13.5556925350ºE 27-MAR-62 00:00:00 2208.000000 T04220
 *         T04
 * 
 *         We detect compegps by looking for G at the beginning of the first row
 * 
 *         Format 3: OziExplorer
 * 
 *         OziExplorer Waypoint File Version 1.0 WGS 84 Reserved 2 Reserved 3
 *         1,L00007 , 45.930833, 13.710833,40140.8923032,18, 1, 3, 0,
 *         65535,L00007 OKROGLICA , 0, 0, 0, 246 2,L01058 , 45.963617,
 *         13.723333,40140.8923032,18, 1, 3, 0, 65535,L01058 LIJAK , 0, 0, 0,
 *         1913
 */
public class WPTImporter {

	/**
	 * Debugging tag
	 */
	@SuppressWarnings("unused")
	private static final String TAG = "WPTImporter";

	WaypointDB db;

	public WPTImporter(WaypointDB db) {
		this.db = db;
	}

	private enum Encoding {
		CompeGPS, Jug, OziExplorer, Cambridge
	};

	/**
	 * 
	 * @param s
	 * @return number of waypoints found
	 * @throws Exception
	 */
	public int addFromStream(InputStream s) throws Exception {
		BufferedReader r = new BufferedReader(new InputStreamReader(s));

		int numFound = 0;
		Encoding encoding = Encoding.Jug;

		// For this format the only row that matters is:
		// W T04220 A 46.3678156439ºN 13.5556925350ºE 27-MAR-62 00:00:00
		// 2208.000000 T04220 T04
		// I use \\s rather than the deg symbol because it seems like the deg
		// symbol mismatches
		String pattern = "^W\\s+(\\S+)\\s+\\S+\\s+([\\d\\.]+)\\S(\\S)\\s+([\\d\\.]+)\\S(\\S)\\s+\\S+\\s+\\S+\\s+(-?[\\d\\.]+)\\s+(.*)";
		Pattern compeRegex = Pattern.compile(pattern);
		
		//Definition taken from http://www.cambridge-aero.com/manuals/303_v2.pdf 
		// which states  Latitude (Degrees, decimal minutes only!) however
		// during testing, I found many files out there in the wild which use
		// degrees:minutes:seconds, so I modified the regex to allow for either.
		// This is as per the standard :
		// 37,44:28.400N,072:40.880W,725F,T, Stowe VT ,ChurchSteepl
		// This is with deg:min:sec :
		// 1,28:16:12S,149:29:49E,950F,AT,AF01        ,Airfield    
		pattern = "(?:\\d{1,}),(?:(\\d{1,3}):){1}(?:(\\d{1,3}(?:\\.\\d{1,}){0,1})){1}(?::(\\d{1,3}(?:\\.\\d{1,}){0,1})){0,1}([NS]),(?:(\\d{1,3}):){1}(?:(\\d{1,3}(?:\\.\\d{1,}){0,1})){1}(?::(\\d{1,3}(?:\\.\\d{1,}){0,1})){0,1}([EW]),(\\d{0,})([FM]),([TSFALH]{0,6}),([\\w\\W]{0,12}),([\\w\\W]{0,12})$";
		Pattern cambridgeRegex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
		String delimiters = null; // If not null we will use this as our
		// delimiter for string parsing
		Matcher cm;
		try {
			String line;

			// We read the name cache once (for speed, because it will get
			// repeatedly generated)
			SortedMap<String, ExtendedWaypoint> names = db.getNameCache();

			while ((line = r.readLine()) != null) {
				line = line.trim();
				try { /* Added a try/catch block here to enable the importer to continue importing after a malformed waypoint is found in the file. */
					if (!line.startsWith("$")) {
						StringTokenizer splitter = (delimiters == null)
								? new StringTokenizer(line)
								: new StringTokenizer(line, delimiters);
	
						String token = splitter.nextToken();
	
						// Determine the file type by the first line
						if (numFound == 0)
							if (token.equals("G")) {
								encoding = Encoding.CompeGPS;
								continue;
							} else if (token.equals("OziExplorer")) {
								encoding = Encoding.OziExplorer;
								delimiters = ","; // csv for the lines we care about
								r.readLine(); // Ignore the next three lines
								r.readLine();
								r.readLine();
								continue;
							} else 
							{ 
								while (line.trim().startsWith("**"))
									line = r.readLine();
								cm = cambridgeRegex.matcher(line);
								if (cm.matches())
									encoding = Encoding.Cambridge;
							}
	
						double latitude = 0, longitude = 0, altitude = 0;
						String name = null, desc = null;
						Waypoint.Type type = Waypoint.Type.Unknown;
						switch (encoding) {
						case Jug:
							// Non compegps files look like:
							// Ignore comments
							// $FormatGEO
							// 49917 N 36 46 53.16 W 119 07 17.09 1511 HILL 49917
							// 4LNTRN N 37 03 54.42 W 119 27 24.00 645 4 LANE TURN
							// AIRSTR N 36 42 59.34 W 119 08 15.54 641 AIRSTRIP
	
							name = token;
	
							boolean latpos = splitter.nextToken().charAt(0) == 'N';
							int latdeg = Integer.parseInt(splitter.nextToken());
							int latmin = Integer.parseInt(splitter.nextToken());
							float latsecs = Float.parseFloat(splitter.nextToken());
							latitude = LocationUtils.DMSToDegrees(latdeg, latmin, latsecs, latpos);
	
							boolean longpos = splitter.nextToken().charAt(0) == 'E';
							int longdeg = Integer.parseInt(splitter.nextToken());
							int longmin = Integer.parseInt(splitter.nextToken());
							float longsecs = Float.parseFloat(splitter.nextToken());
							longitude = LocationUtils.DMSToDegrees(longdeg, longmin, longsecs, longpos);
	
							altitude = Float.parseFloat(splitter.nextToken()); // In
																				// meters
	
							desc = splitter.nextToken("").trim();
							break;
						case CompeGPS:
							// For this format the only row that matters is:
							// W T04220 A 46.3678156439ºN 13.5556925350ºE 27-MAR-62
							// 00:00:00 2208.000000 T04220 T04
							Matcher m = compeRegex.matcher(line);
							if (m.find()) {
								name = m.group(1);
								String latStr = m.group(2), latNS = m.group(3), longStr = m.group(4), longEW = m
										.group(5), altStr = m.group(6);
								desc = m.group(7);
	
								latitude = Double.parseDouble(latStr) * (latNS.equals("N") ? 1 : -1);
								longitude = Double.parseDouble(longStr) * (longEW.equals("E") ? 1 : -1);
								altitude = 0.3048 * Double.parseDouble(altStr); // in
																				// feet
																				// according
																				// to
																				// spec
							}
							break;
						case OziExplorer:
							// Look for lines like:
							// "1,L00007 , 45.930833, 13.710833,40140.8923032,18, 1, 3, 0, 65535,L00007 OKROGLICA , 0, 0, 0, 246"
	
							// the current token should contain the wpt # (which we
							// ignore)
	
							String[] splitted = line.split(",");
							// Log.d("x", splitted.toString());
							name = splitted[1].trim();
							latitude = Double.parseDouble(splitted[2].trim());
							longitude = Double.parseDouble(splitted[3].trim());
							desc = splitted[10].trim();
	
							// For some files the trailing part of the description
							// might have hyphens that we should skip
							// FIXME, scan from tail of string and remove them
							// for example of this see
							// http://parapente.ffvl.fr/compet/1398/balises
							altitude = 0.3048 * Double.parseDouble(splitted[14].trim()); // in
																							// feet
																							// according
																							// to
																							// spec
							break;
						case Cambridge:
							// Each Waypoint is one line of text. Commas separate fields in the database.
							// 37,44:28.400N,072:40.880W,725F,T, Stowe VT ,ChurchSteepl
							// 38,44:06.920N,072:49.820W,1470F,TAH, Sugarbush ,RW 22 NE End
							// 39,44:24.500N,072:11.700W,1450F,T, W. Danville, Lake Narrow
							// 1 2 3 4 5 6 7
							// Field # Meaning
							// 0 Waypoint Index #
							// 1 Latitude (Degrees, decimal minutes only!) (N=North, S=South)
							// 2 Longitude (W=Western Hemisphere, E=Eastern Hemisphere)
							// 3 Elevation (F = Feet, M = Meters)
							// 4 Attributes (Each point can have up to 6 attributes)
							// 5 Waypoint Name (Up to 12 characters)
							// 6 Comment Field (Up to 12 characters)
							cm = cambridgeRegex.matcher(line);
							if (cm.find()) {
								String latDStr = cm.group(1), 
										latMstr = cm.group(2),
										latSstr = cm.group(3),
										latNS = cm.group(4), 
										lonDStr = cm.group(5),
										lonMstr = cm.group(6),
										lonSstr = cm.group(7),
										lonEW = cm.group(8), 
										altStr = cm.group(9), 
										altUnits = cm.group(10),
										Attributes = cm.group(11);
								if (Attributes.contains("A"))
									type = Waypoint.Type.Launch;
								else if (Attributes.contains("L"))
									type = Waypoint.Type.Landing;
								else if (Attributes.contains("T"))
									type = Waypoint.Type.Turnpoint;
								else 
									type = Waypoint.Type.Unknown;
								name = cm.group(12).trim();
								desc = cm.group(13).trim();
								float latM = 0, latS = 0, lonM = 0 , lonS = 0;
								if (latMstr != null)
									latM = Float.parseFloat(latMstr);
								if (latSstr != null)
									latS = Float.parseFloat(latSstr);
								if (lonMstr != null)
									latS = Float.parseFloat(lonMstr);
								if (lonSstr != null)
									lonS = Float.parseFloat(lonSstr);
								
								latitude = LocationUtils.DMSToDegrees(Integer.parseInt(latDStr), latM, latS, latNS.equals("N"));
								longitude = LocationUtils.DMSToDegrees(Integer.parseInt(lonDStr), lonM, lonS, lonEW.equals("E"));
								if (altUnits == "F")
									altitude = Double.parseDouble(altStr) ;
								else
									altitude = Double.parseDouble(altStr) / 3.2808399;
							}
							break;
						}
						// Did we find anything?
						if (desc != null) {
							// Check for common abbreviations
							if (type == Waypoint.Type.Unknown)
							{
								String descname = desc.toLowerCase() + name.toLowerCase();
								if (descname.contains("lz") || descname.contains("landing") || descname.contains("lp")) // FIXME, make this
									// localizable
									type = Waypoint.Type.Landing;
								else if (descname.contains("launch") || descname.contains("sp"))
									type = Waypoint.Type.Launch;
							}
							// If the user specified a name, replace any existing
							// items
							// with the
							// same name
							// FIXME - figure out how to have SQL take care of
							// merging/replacing
							// existing items
							ExtendedWaypoint existingWaypoint;
							if (name.length() != 0 && (existingWaypoint = names.get(name)) != null) {
								existingWaypoint.latitude = latitude;
								existingWaypoint.longitude = longitude;
								existingWaypoint.altitude = (int) altitude;
								existingWaypoint.type = type;
								if (!desc.equals(name) && desc.length() != 0) // A
									// lot
									// of
									// saved
									// wpt
									// files
									// have
									// redundant
									// entries
									// for
									// description
									existingWaypoint.description = desc;
	
								existingWaypoint.commit();
							} else {
								ExtendedWaypoint w = new ExtendedWaypoint(name, latitude, longitude,
										(int) altitude, type
												.ordinal());
								if (!desc.equals(name) && desc.length() != 0)
									w.description = desc; // Optional
								db.add(w);
							}
	
							numFound++;
						}
					}
				}
				catch( Exception ex )
				{ }
			}
		} finally {
			r.close();
		}

		return numFound;
	}
}
