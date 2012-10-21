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

import java.util.StringTokenizer;

import android.util.Log;

import com.geeksville.location.LocationUtils;
import com.geeksville.location.WaypointDB;

public class Jug extends Parse {

	public Jug(String fileContents, WaypointDB db)
	{
		super(db, fileContents);
	}
	@Override
	public int Find() {
		String[] s = mFileContents.split("\n");
		int i = 0;
		while(!s[i].startsWith("$FormatGEO") )
			i++;
		i++;
		for (i = i; i < s.length; i++) {
			while(s[i].startsWith("#") ) // Skip commented lines
				i++;
			try{
				StringTokenizer splitter = new StringTokenizer(s[i]);
				String token = splitter.nextToken();
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
	
				altitude = Float.parseFloat(splitter.nextToken()); // In meters
	
				description = splitter.nextToken("").trim();
				Save();
				numFound ++;
			}
			catch(Exception ex)
			{
				Log.d(Tag, s[i]);
			}
		}
		return numFound;
	}

}

/*
// Non compegps files look like:
// Ignore comments
// $FormatGEO
// 49917 N 36 46 53.16 W 119 07 17.09 1511 HILL 49917
// 4LNTRN N 37 03 54.42 W 119 27 24.00 645 4 LANE TURN
// AIRSTR N 36 42 59.34 W 119 08 15.54 641 AIRSTRIP


break;

*/