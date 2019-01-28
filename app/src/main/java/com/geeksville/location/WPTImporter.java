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

import com.geeksville.location.parse.CambridgeDat;
import com.geeksville.location.parse.CompeGPS;
import com.geeksville.location.parse.Jug;
import com.geeksville.location.parse.OziExplorer;
import com.geeksville.location.parse.Parse;
import com.geeksville.location.parse.SeeYouCUP;

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
	public String fileContents;
	/**
	 * 
	 * @param s
	 * @return number of waypoints found
	 * @throws Exception
	 */
	public int addFromStream(InputStream s) throws Exception {
		BufferedReader r = new BufferedReader(new InputStreamReader(s));
		StringBuilder sb = new StringBuilder(); 
		String line = null;
		final String newline = "\n";
		if ((line = r.readLine()) != null) // This construct avoids adding a superfluous newline at the end of the string.
			{
			sb.append(line);
			while ((line = r.readLine()) != null) {
				sb.append(newline);
				sb.append(line);
			}
		}
		fileContents = sb.toString();
		
		Parse parser;
				
		parser = new CambridgeDat(fileContents, db);
		if (parser.Find() == 0)
		{
			parser = new CompeGPS(fileContents,db);
			if (parser.Find() == 0)
			{ 
				parser = new OziExplorer(fileContents,db);
				if (parser.Find() == 0)
				{ 
					parser = new SeeYouCUP(fileContents,db);
					if (parser.Find() == 0)
					{
						parser = new Jug(fileContents,db);
						parser.Find();
					}
				}
			}
		}
		return parser.numFound;
	}
}
