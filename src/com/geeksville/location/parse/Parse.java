package com.geeksville.location.parse;

import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.geeksville.location.ExtendedWaypoint;
import com.geeksville.location.Waypoint;
import com.geeksville.location.WaypointDB;

abstract public class Parse {
	WaypointDB db;
	public Parse(WaypointDB db, String fileContents, String regexPattern) {
		this(db, fileContents);
		pattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE); 
		matcher = pattern.matcher(mFileContents);
	}
	public Parse(WaypointDB db, String fileContents)
	{
		this.db = db;
		mFileContents = fileContents;
		Tag = this.getClass().getName();
	}
	protected double latitude, longitude, altitude;
	protected String name = "", description = ""; 
	protected Waypoint.Type type = Waypoint.Type.Unknown;
	protected Pattern pattern;
	protected Matcher matcher;
	protected String mFileContents;
	public int numFound = 0;
	final protected String Tag;
	abstract public int Find();
	protected void Save()
	{
		SortedMap<String, ExtendedWaypoint> names = db.getNameCache();
		// Did we find anything?
		if (description != null) {
			// Check for common abbreviations
			if (type == Waypoint.Type.Unknown)
			{
				String descname = description.toLowerCase() + name.toLowerCase();
				if (descname.contains("lz") || descname.contains("landing") || descname.contains("lp")) // FIXME, make this 
																											// localizable
					type = Waypoint.Type.Landing;
				else if (descname.contains("launch") || descname.contains("sp"))
					type = Waypoint.Type.Launch;
			}
			// If the user specified a name, replace any existing itemswith the same name
			// FIXME - figure out how to have SQL take care of
			// merging/replacing existing items
			ExtendedWaypoint existingWaypoint;
			if (name.length() != 0 && (existingWaypoint = names.get(name)) != null) {
				existingWaypoint.latitude = latitude;
				existingWaypoint.longitude = longitude;
				existingWaypoint.altitude = (int) altitude;
				existingWaypoint.type = type;
				if (!description.equals(name) && description.length() != 0) // A lot of saved wpt files have redundant entries for description
					existingWaypoint.description = description;

				existingWaypoint.commit();
			} else {
				ExtendedWaypoint w = new ExtendedWaypoint(name, latitude, longitude,
						(int) altitude, type
								.ordinal());
				if (!description.equals(name) && description.length() != 0)
					w.description = description; // Optional
				db.add(w);
			}
		}
	}
	
}
