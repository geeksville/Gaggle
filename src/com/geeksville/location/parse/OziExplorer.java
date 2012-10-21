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

import com.geeksville.location.Waypoint;
import com.geeksville.location.WaypointDB;

public class OziExplorer extends Parse {

	public OziExplorer(String fileContents, WaypointDB db)
	{
		super(db, fileContents);
	}
	@Override
	public int Find() {
		String[] s = mFileContents.split("\n");
		if (s[0].startsWith("OziExplorer"))
			for (int i = 4; i < s.length; i++) {
				try{
					String[] splitted = s[i].split(",");
					name = splitted[1].trim();
					latitude = Double.parseDouble(splitted[2].trim());
					longitude = Double.parseDouble(splitted[3].trim());
					description = splitted[10].trim();
					// For some files the trailing part of the description
					// might have hyphens that we should skip
					// FIXME, scan from tail of string and remove them
					// for example of this see
					// http://parapente.ffvl.fr/compet/1398/balises
					altitude = 0.3048 * Double.parseDouble(splitted[14].trim());
					int Symbol = Integer.parseInt(splitted[5].trim());
					switch (Symbol)
					{
						case 0: case 27: case 69: 
							// airport, glider, ultralight
							type = Waypoint.Type.Landing;
							break;
						case 70:case 83:case 140: case 141: case 142: case 143: case 144: case 145:			
							// WAYPONIT, flag, red flag, blue flag, green flag, red pin, blue pin, green pin,
							type = Waypoint.Type.Turnpoint;
							break;
						default:
							type = Waypoint.Type.Unknown;
							break;
					}
					Save();
					numFound ++;
				}
				catch(Exception Ex)
				{
					Log.d(Tag, s[i]);
				}
			}
		return numFound;
	}

}
