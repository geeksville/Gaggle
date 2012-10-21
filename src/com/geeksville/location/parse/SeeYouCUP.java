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

import com.geeksville.location.LocationUtils;
import com.geeksville.location.Waypoint;
import com.geeksville.location.WaypointDB;

public class SeeYouCUP extends Parse {
	
		//as per specification at http://download.naviter.com/docs/cup_format.pdf
		//"Lesce-Bled","LESCE",SI,4621.666N,01410.332E,505.0m,2,130,1140.0m,"123.50","Home airfield"
	//http://stackoverflow.com/questions/1441556/parsing-csv-input-with-a-regex-in-java
	//http://geekswithblogs.net/mwatson/archive/2004/09/04/10658.aspx
	public SeeYouCUP(String fileContents, WaypointDB db) {
		super(db, fileContents,  "^(?:\"{0,1}([^\"]*)\"{0,1},)(?:\"{0,1}[^\"]*\"{0,1},)(?:[^,]*,)(?:([\\d]{2})([\\d]{2}).([\\d]{3})([NS]),)(?:([\\d]{3})([\\d]{2}).([\\d]{3})([EW]),)(?:([\\d\\.]*)(m|ft),)(\\d),(?:[^,]*,)(?:[^,]*,)(?:[^,]*,)(?:\"{0,1}([^\"]*)\"{0,1})$|-----Related\\sTasks-----");
				
	}

	@Override
	public int Find() {
		while (matcher.find())
			{	
			try{
				if (matcher.group().contains("-----Related Tasks-----"))
					break;
				String latDStr = matcher.group(2), 
				latMstr = matcher.group(3),
				latMDecimalStr = matcher.group(4),
				latNS = matcher.group(5), 
				lonDStr = matcher.group(6),
				lonMstr = matcher.group(7),
				lonMDecimalStr = matcher.group(8),
				lonEW = matcher.group(9), 
				altStr = matcher.group(10), 
				altUnits = matcher.group(11),
				waypointStyle= matcher.group(12);
				int wpStyle = Integer.parseInt(waypointStyle);
				if(wpStyle>1 && wpStyle< 6)
					type = Waypoint.Type.Landing;
				else  
					type = Waypoint.Type.Unknown;
				name = matcher.group(1).trim();
				description = matcher.group(13).trim();
				float latM = 0, latS = 0, lonM = 0 , lonS = 0;
				if (latMstr != null)
					latM = Float.parseFloat(latMstr + "." + latMDecimalStr);
				if (lonMstr != null)
					lonM = Float.parseFloat(lonMstr + "." +  lonMDecimalStr);
				
				latitude = LocationUtils.DMSToDegrees(Integer.parseInt(latDStr), latM, latS, latNS.equals("N"));
				longitude = LocationUtils.DMSToDegrees(Integer.parseInt(lonDStr), lonM, lonS, lonEW.equals("E"));
				if (altUnits.toLowerCase() == "ft")
					altitude = Double.parseDouble(altStr) ;
				else
					altitude = Double.parseDouble(altStr) / 3.2808399;
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
