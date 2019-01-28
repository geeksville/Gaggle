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
