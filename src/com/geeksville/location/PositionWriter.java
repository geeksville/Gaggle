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

/**
 * 
 * @author kevinh
 * 
 */
public interface PositionWriter {

	/**
	 * Spit out the stuff that is needed at the beginning of the file
	 */
	void emitProlog();

	/**
	 * Store a position record in the file
	 * 
	 * @param time
	 *            number of milliseconds since 1970 (unix time)
	 * @param altitude
	 *            in meters, NaN for unknown
	 * @param bearing
	 *            the ground track in degrees from 0-359
	 * @param groundSpeed
	 *            in km/hr
	 * @param accel
	 *            TODO
	 * @param vspd TODO
	 */
	void emitPosition(long time, double latitude, double longitude, float altitude, int bearing,
			float groundSpeed, float[] accel, float vspd);

	/**
	 * Add standard end of file stuff
	 */
	void emitEpilog();
}
