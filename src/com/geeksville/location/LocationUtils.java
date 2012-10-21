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
		int degOut = (int) degIn;
		double minutes = 60 * (degIn - degOut);
		int minwhole = (int) minutes;
		double seconds = ((minutes - minwhole) * 60);

		return new String[] { Integer.toString(degOut), Integer.toString(minwhole),
				Double.toString(seconds),
				Character.toString(dirLetter) };
	}
	public static String[] degreesToDM(double degIn, boolean isLatitude) {
		boolean isPos = degIn >= 0;
		char dirLetter = isLatitude ? (isPos ? 'N' : 'S') : (isPos ? 'E' : 'W');

		degIn = Math.abs(degIn);
		int degOut = (int) degIn;
		double minutes = 60 * (degIn - degOut);
		int seconds = 0;

		return new String[] { Integer.toString(degOut), Double.toString(minutes),
				Integer.toString(seconds),
				Character.toString(dirLetter) };
	}

	public static String[] degreesToD(double degIn, boolean isLatitude) {
		boolean isPos = degIn >= 0;
		char dirLetter = isLatitude ? (isPos ? 'N' : 'S') : (isPos ? 'E' : 'W');

		degIn = Math.abs(degIn);
		double degOut = degIn;
		int minutes = 0;
		int seconds = 0;

		return new String[] { Double.toString(degOut), Integer.toString(minutes),
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
	
	public static double LatLongToMeter(double lat_a, double lng_a, double lat_b, double lng_b) {
		double pk = (double) (180 / 3.14169);

		double a1 = lat_a / pk;
		double a2 = lng_a / pk;
		double b1 = lat_b / pk;
		double b2 = lng_b / pk;

		double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
		double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
		double t3 = Math.sin(a1) * Math.sin(b1);
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
	public static double DMSToDegrees(double degrees, double minutes, double seconds, boolean isPostive) {
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
		int vspdCol = pts
				.getColumnIndexOrThrow(LocationLogDbAdapter.KEY_LOC_VSPD);

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

			float vspd = pts.isNull(vspdCol) ? Float.NaN : pts
					.getFloat(vspdCol);

			dest.emitPosition(time, latitude, longitude, altitude, bearing,
					groundSpeed, accel, vspd);

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
