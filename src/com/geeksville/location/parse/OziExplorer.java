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
