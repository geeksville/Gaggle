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