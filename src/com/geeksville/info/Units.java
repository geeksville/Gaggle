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
package com.geeksville.info;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Convert units based on the user's preferences
 * 
 * @author kevinh
 * 
 */
public class Units {

	public static Units instance = new Units();

	/**
	 * You must be careful to keep these enums in sync with the preferences
	 * arrays.xml
	 * 
	 * @author kevinh
	 * 
	 */
	public enum Speed {
		/**
		 * mile per hour
		 */
		MilePerHr,

		/**
		 * km per hour
		 */
		KmPerHr,

		/**
		 * Meters per sec
		 */
		MPerSec,

		/**
		 * Feet per minute
		 */
		FeetPerMin,

		/**
		 * Knots per hr
		 */
		KtsPerHr,
	}

	private String getSpeedUnits(Speed s) {
		final String[] speedUnits = { "mph", "kph", "m/s", "fpm",
				"kts/hr" };

		return speedUnits[s.ordinal()];
	}

	// FIXME - fill in missing #s
	final double[] speedScaling = { 2.2369, 3.6, 1, 196.8503, 1.94384 };

	public enum Distance {
		Meters, Feet, KM, Miles, NauticalMiles
	}
	public enum CoordinateSet
	{
		DMS, DM, D
	}
	private String getDistanceUnits(Distance d) {
		final String[] distanceUnits = { "m", "'", "km", "mi", "Nm" };

		return distanceUnits[d.ordinal()];
	}

	final double[] distanceScaling = { 1.0, 3.2808399, 0.001, 0.000621371192,
			0.000539956803 };

	/**
	 * Convert mks units to whatever the selected units are
	 * 
	 * @param d
	 * @param meters
	 * @return
	 */
	private <ScaleType extends Enum> double toSelectedUnits(ScaleType d,
			double[] scaling, double meters) {
		return meters * scaling[d.ordinal()];
	}

	/**
	 * Convert whatever the selected distance units to mks units
	 * 
	 * @param d
	 * @param meters
	 * @return
	 */
	private <ScaleType extends Enum> double fromSelectedUnits(ScaleType d,
			double[] scaling, double meters) {
		return meters / scaling[d.ordinal()];
	}

	private Distance vdistance = Distance.Feet;
	private Distance hdistance = Distance.KM;
	private Speed hspeed = Speed.KmPerHr;
	private Speed vspeed = Speed.MPerSec;

	/**
	 * Pull our changes from the prefs (FIXME, is there something I can
	 * subscribe to for this?)
	 * 
	 * @param c
	 */
	public void setFromPrefs(Context c) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);

		String defval = Distance.Feet.toString();
		vdistance = Distance.valueOf(prefs.getString("altunits1_pref", defval));
		hdistance = Distance.valueOf(prefs.getString("distunits1_pref",
				Distance.Miles.toString()));
		hspeed = Speed.valueOf(prefs.getString("speedunits1_pref",
				Speed.MilePerHr.toString()));
		vspeed = Speed.valueOf(prefs.getString("vspdunits1_pref",
				Speed.FeetPerMin.toString()));
	}

	public String getAltitudeUnits() {
		return getDistanceUnits(vdistance);
	}

	public String metersToAltitude(double alt) {
		return String.format("%d", (int) toSelectedUnits(vdistance,
				distanceScaling, alt));
	}

	/**
	 * Convert from the user's preferred altitude to meters
	 * 
	 * @param alt
	 * @return
	 */
	public double altitudeToMeters(double alt) {
		return fromSelectedUnits(vdistance, distanceScaling, alt);
	}

	public String getDistanceUnits() {
		return getDistanceUnits(hdistance);
	}

	public String metersToDistance(double v) {
		// FIXME - automatically switch to meters if less than some distance

		v = toSelectedUnits(hdistance, distanceScaling, v);

		// Only show fractions if we are close
		return String.format(v < 10 ? "%.2f" : "%.0f", v);
	}

	public String getSpeedUnits() {
		return getSpeedUnits(hspeed);
	}

	public String getVSpeedUnits() {
		return getSpeedUnits(vspeed);
	}

	public String meterPerSecToSpeed(double v) {
		return String.format("%.1f", toSelectedUnits(hspeed, speedScaling, v));
	}
	
	public String kilometerPerHourToSpeed(double v) {
		double meters =  v / speedScaling[Speed.KmPerHr.ordinal()];
		return String.format("%.1f", toSelectedUnits(hspeed, speedScaling, meters));
	}

	public String meterPerSecToVSpeed(double v) {
		return String.format("%.1f", toSelectedUnits(vspeed, speedScaling, v));
	}

	/**
	 * generate the user's preferred degrees, minutes, seconds string
	 * 
	 * @param degs
	 * @return
	 * 
	 *         FIXME: Offer a few choices of how to format these strings. For
	 *         now, deg & fractional min
	 */
	public String degreesToUserString(double degs, boolean isLatitude) {
		// TODO Auto-generated method stub
		return degreesToDegreeMinutesSeconds(degs, isLatitude);
	}

	/**
	 * Format as degrees, minutes, secs
	 * 
	 * @param degIn
	 * @param isLatitude
	 * @return a string like 120deg
	 */
	private static String degreesToDegreeMinutesSeconds(double degIn, boolean isLatitude) {
		boolean isPos = degIn >= 0;
		char dirLetter = isLatitude ? (isPos ? 'N' : 'S') : (isPos ? 'E' : 'W');

		degIn = Math.abs(degIn);
		int degFloor = (int) degIn;
		double minutes = 60 * (degIn - degFloor);
		int minwhole = (int) minutes;
		int seconds = (int) ((minutes - minwhole) * 60);

		// 00b0 is unicode for the degree symbol
		String s = String.format("%d\u00B0%02d'%02d\"%c", degFloor,
				minwhole, seconds, dirLetter);
		return s;
	}
}
