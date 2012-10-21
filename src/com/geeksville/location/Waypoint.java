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
package com.geeksville.location;

import android.database.Cursor;

/**
 * A space efficient waypoint that doesn't use floatingpoint math
 * 
 * @author kevinh
 * 
 *         This class should only contain _persistent_ waypoint state. For
 *         transient state that can be calculated/cached see ExtendedWaypoint
 */
public class Waypoint {

	public long id;

	public String name, description;

	public double latitude, longitude;

	public int altitude;

	public enum Type {
		Unknown, Landing, Launch, Turnpoint
	};

	/**
	 * A type for this waypoint
	 */
	public Type type;

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param latitude
	 * @param longitude
	 * @param altitude
	 * @param diameter
	 */
	public Waypoint(String name, double latitude, double longitude, int altitude, int type) {
		this.id = -1; // No id until we end up in a DB
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
		this.type = Type.values()[type];
	}

	/**
	 * We assume we only have one set of waypoint columns
	 */
	private static int latCol = -1;
	private static int longCol;
	private static int altCol;
	private static int nameCol, descCol, typeCol, idCol;

	/**
	 * Generate a waypoint from the current db cursor position
	 * 
	 * @param pts
	 */
	public Waypoint(Cursor pts) {
		// Find the column mapping once
		if (latCol == -1) {
			latCol = pts.getColumnIndexOrThrow(LocationLogDbAdapter.KEY_LATITUDE);
			longCol = pts.getColumnIndexOrThrow(LocationLogDbAdapter.KEY_LONGITUDE);
			altCol = pts.getColumnIndexOrThrow(LocationLogDbAdapter.KEY_ALTITUDE);
			nameCol = pts.getColumnIndexOrThrow(LocationLogDbAdapter.KEY_NAME);
			descCol = pts.getColumnIndexOrThrow(LocationLogDbAdapter.KEY_DESCRIPTION);
			typeCol = pts.getColumnIndexOrThrow(LocationLogDbAdapter.KEY_WAYPOINT_TYPE);
			idCol = pts.getColumnIndexOrThrow(LocationLogDbAdapter.KEY_ROWID);
		}

		id = pts.getLong(idCol);
		latitude = pts.getDouble(latCol);
		longitude = pts.getDouble(longCol);
		altitude = pts.getInt(altCol);
		name = pts.getString(nameCol);
		description = pts.getString(descCol);
		type = Type.values()[pts.getInt(typeCol)];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "WP:" + name;
	}

}
