package com.geeksville.location.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

import com.geeksville.location.LocationUtils;
import com.geeksville.location.Waypoint;
import com.geeksville.location.WaypointDB;


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

public class CambridgeDat extends Parse {
	public CambridgeDat(String fileContents, WaypointDB db)
	{
		super(db, fileContents,  "^(?:\\d{1,}),(?:(\\d{1,3}):){1}(?:(\\d{1,3}(?:\\.\\d{1,}){0,1})){1}(?::(\\d{1,3}(?:\\.\\d{1,}){0,1})){0,1}([NS]),(?:(\\d{1,3}):){1}(?:(\\d{1,3}(?:\\.\\d{1,}){0,1})){1}(?::(\\d{1,3}(?:\\.\\d{1,}){0,1})){0,1}([EW]),(\\d{0,})([FM]),([TSFALH]{0,6}),([\\w\\W]{0,12}),([\\w\\W]{0,12})$");
	}
	@Override
	public int Find()
	{
		while (matcher.find())
			{	
			try{
				String latDStr = matcher.group(1), 
				latMstr = matcher.group(2),
				latSstr = matcher.group(3),
				latNS = matcher.group(4), 
				lonDStr = matcher.group(5),
				lonMstr = matcher.group(6),
				lonSstr = matcher.group(7),
				lonEW = matcher.group(8), 
				altStr = matcher.group(9), 
				altUnits = matcher.group(10),
				Attributes = matcher.group(11);
				if (Attributes.contains("A"))
					type = Waypoint.Type.Launch;
				else if (Attributes.contains("L"))
					type = Waypoint.Type.Landing;
				else if (Attributes.contains("T"))
					type = Waypoint.Type.Turnpoint;
				else 
					type = Waypoint.Type.Unknown;
				name = matcher.group(12).trim();
				description = matcher.group(13).trim();
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
				if (altUnits.toLowerCase() == "f")
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
