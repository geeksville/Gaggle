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
			// If the user specified a name, replace any existing items with the same name
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
