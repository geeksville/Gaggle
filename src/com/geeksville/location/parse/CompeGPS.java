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
package com.geeksville.location.parse;

import android.util.Log;

import com.geeksville.location.WaypointDB;

// For this format the only row that matters is:
// W T04220 A 46.3678156439ºN 13.5556925350ºE 27-MAR-62 00:00:00
// 2208.000000 T04220 T04
// I use \\s rather than the deg symbol because it seems like the deg
// symbol mismatches

public class CompeGPS extends Parse {

	public CompeGPS(String fileContents, WaypointDB db)
	{
		super(db, fileContents, "^W\\s+(\\S+)\\s+\\S+\\s+([\\d\\.]+)\\S(\\S)\\s+([\\d\\.]+)\\S(\\S)\\s+\\S+\\s+\\S+\\s+(-?[\\d\\.]+)\\s+(.*)");
	}
	@Override
	public int Find() {
		while (matcher.find())
			{	
			try{
				name = matcher.group(1);
				String 	latStr = matcher.group(2), 
						latNS = matcher.group(3), 
						longStr = matcher.group(4), 
						longEW = matcher.group(5), 
						altStr = matcher.group(6);
				description = matcher.group(7);

				latitude = Double.parseDouble(latStr) * (latNS.equals("N") ? 1 : -1);
				longitude = Double.parseDouble(longStr) * (longEW.equals("E") ? 1 : -1);
				altitude = 0.3048 * Double.parseDouble(altStr);
				Save();
				numFound ++;
			}
			catch(Exception ex)
			{
				Log.d(Tag, matcher.group());
			}
		}
		return numFound;
	}

}

/*
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
break;*/