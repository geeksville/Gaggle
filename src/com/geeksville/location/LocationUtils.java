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
import android.util.FloatMath;

public final class LocationUtils {

	/**
	 * Format as degrees, minutes, secs
	 * 
	 * @param degIn
	 * @param isLatitude
	 * @return a string like 120deg
	 */
	public static String[] degreesToDMS(double degIn, boolean isLatitude) {
		boolean isPos = degIn >= 0;
		char dirLetter = isLatitude ? (isPos ? 'N' : 'S') : (isPos ? 'E' : 'W');

		degIn = Math.abs(degIn);
		int degFloor = (int) degIn;
		double minutes = 60 * (degIn - degFloor);
		int minwhole = (int) minutes;
		int seconds = (int) ((minutes - minwhole) * 60);

		return new String[] { Integer.toString(degFloor), Integer.toString(minwhole),
				Integer.toString(seconds),
				Character.toString(dirLetter) };
	}

	/**
	 * A not super efficent mapping from a starting lat/long + a distance at a
	 * certain direction
	 * 
	 * @param lat
	 * @param longitude
	 * @param distMeters
	 * @param theta
	 *            in radians, 0 == north
	 * @return an array with lat and long
	 */
	public static double[] addDistance(double lat, double longitude, double distMeters, double theta) {
		double dx = distMeters * Math.sin(theta); // theta measured clockwise
		// from due north
		double dy = distMeters * Math.cos(theta); // dx, dy same units as R

		double dLong = dx / (111320 * Math.cos(lat)); // dx, dy in meters
		double dLat = dy / 110540; // result in degrees long/lat

		return new double[] { lat + dLat, longitude + dLong };
	}

	/**
	 * Convert distance between two lat/long points into meters
	 * 
	 * @param lat_a
	 * @param lng_a
	 * @param lat_b
	 * @param lng_b
	 * @return
	 */
	public static double LatLongToMeter(float lat_a, float lng_a, float lat_b, float lng_b) {
		float pk = (float) (180 / 3.14169);

		float a1 = lat_a / pk;
		float a2 = lng_a / pk;
		float b1 = lat_b / pk;
		float b2 = lng_b / pk;

		float t1 = FloatMath.cos(a1) * FloatMath.cos(a2) * FloatMath.cos(b1) * FloatMath.cos(b2);
		float t2 = FloatMath.cos(a1) * FloatMath.sin(a2) * FloatMath.cos(b1) * FloatMath.sin(b2);
		float t3 = FloatMath.sin(a1) * FloatMath.sin(b1);
		double tt = Math.acos(t1 + t2 + t3);

		if (Double.isNaN(tt))
			tt = 0; // Must have been the same point?

		return 6366000 * tt;
	}

	/**
	 * Convert degrees/mins/secs to a single double
	 * 
	 * @param degrees
	 * @param minutes
	 * @param seconds
	 * @param isPostive
	 * @return
	 */
	public static double DMSToDegrees(int degrees, int minutes, float seconds, boolean isPostive) {
		double r = (isPostive ? 1 : -1) * (degrees + (minutes / 60.0) + (seconds / 3600.0));
		return r;
	}
	public static double DMSToDegrees(int degrees, float minutes, float seconds, boolean isPostive) {
		double r = (isPostive ? 1 : -1) * (degrees + (minutes / 60.0) + (seconds / 3600.0));
		return r;
	}

	
	/**
	 * Utility glue for reading from a DB and writing to a position writer (used
	 * for file export)
	 * 
	 * @param db
	 * @param dest
	 * @param fltId
	 * 
	 *            I didn't want to infect the DB stuff with knowledge of
	 *            position writers, nor position writers with knowledge of DBs.
	 *            So here we are...
	 */
	public static void dbToWriter(LocationLogDbAdapter db, PositionWriter dest, long fltId) {

		// First get all the flight info
		Cursor fltinfo = db.fetchFlight(fltId);

		int nameCol = fltinfo.getColumnIndexOrThrow(LocationLogDbAdapter.KEY_FLT_PILOTNAME);
		int notesCol = fltinfo.getColumnIndexOrThrow(LocationLogDbAdapter.KEY_DESCRIPTION);

		// FIXME - use this info when writing
		String pilotName = fltinfo.getString(nameCol);
		String flightDesc = fltinfo.getString(notesCol);
		fltinfo.close();

		Cursor pts = db.fetchLocations(fltId);

		int latCol = pts.getColumnIndexOrThrow(LocationLogDbAdapter.KEY_LATITUDE);
		int longCol = pts.getColumnIndexOrThrow(LocationLogDbAdapter.KEY_LONGITUDE);
		int altCol = pts.getColumnIndexOrThrow(LocationLogDbAdapter.KEY_ALTITUDE);
		int timeCol = pts.getColumnIndexOrThrow(LocationLogDbAdapter.KEY_LOC_TIME);
		int bearingCol = pts.getColumnIndexOrThrow(LocationLogDbAdapter.KEY_LOC_GNDTRACK);
		int speedCol = pts.getColumnIndexOrThrow(LocationLogDbAdapter.KEY_LOC_GNDSPEED);
		int accxCol = pts.getColumnIndexOrThrow(LocationLogDbAdapter.KEY_LOC_ACCX);
		int accyCol = pts.getColumnIndexOrThrow(LocationLogDbAdapter.KEY_LOC_ACCY);
		int acczCol = pts.getColumnIndexOrThrow(LocationLogDbAdapter.KEY_LOC_ACCZ);

		int numPts = pts.getCount();

		dest.emitProlog();

		float[] accelArray = new float[3];

		for (int i = 0; i < numPts; i++) {
			double latitude = pts.getDouble(latCol);
			double longitude = pts.getDouble(longCol);
			int altitude = pts.getInt(altCol);
			long time = pts.getLong(timeCol);
			int bearing = pts.getInt(bearingCol);
			int groundSpeed = pts.getInt(speedCol);

			float[] accel = accelArray;
			if (pts.isNull(accxCol))
				accel = null; // No accel data available for this point
			else {
				accel[0] = pts.getFloat(accxCol);
				accel[1] = pts.getFloat(accyCol);
				accel[2] = pts.getFloat(acczCol);
			}

			dest.emitPosition(time, latitude, longitude, altitude, bearing, groundSpeed, accel);

			pts.moveToNext();
		}

		pts.close();
		dest.emitEpilog();
	}

	/**
	 * Computes the bearing in degrees between two points on Earth.
	 * 
	 * @param lat1
	 *            Latitude of the first point
	 * @param lon1
	 *            Longitude of the first point
	 * @param lat2
	 *            Latitude of the second point
	 * @param lon2
	 *            Longitude of the second point
	 * @return Bearing between the two points in degrees. A value of 0 means due
	 *         north.
	 */
	public static double bearing(double lat1, double lon1, double lat2, double lon2) {
		double lat1Rad = Math.toRadians(lat1);
		double lat2Rad = Math.toRadians(lat2);
		double deltaLonRad = Math.toRadians(lon2 - lon1);

		double y = Math.sin(deltaLonRad) * Math.cos(lat2Rad);
		double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - Math.sin(lat1Rad) * Math.cos(lat2Rad)
				* Math.cos(deltaLonRad);
		return radToBearing(Math.atan2(y, x));
	}

	/**
	 * Converts an angle in radians to degrees
	 */
	public static double radToBearing(double rad) {
		return (Math.toDegrees(rad) + 360) % 360;
	}

}
