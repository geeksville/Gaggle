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
 * Write locations to an in memory expanding array of points (useful for map
 * view)
 * 
 * @author kevinh
 * 
 */
public class LocationListWriter implements PositionWriter {

	private LocationList dest;
	long startTime = -1;

	/**
	 * Constructor
	 * 
	 * @param dest
	 *            where we are writing to
	 */
	public LocationListWriter(LocationList dest) {
		this.dest = dest;
	}

	@Override
	public void emitEpilog() {
		// Do nothing
	}

	@Override
	public void emitPosition(long time, double latitude, double longitude, float altitude,
			int bearing, float groundSpeed, float[] accel, float vspd) {
		if (startTime == -1)
			startTime = time; // Generate 0 for the first point

		dest.add(latitude, longitude, (int) (altitude * 1000.0), (int) (time - startTime));
	}

	@Override
	public void emitProlog() {
		// Do nothing
	}

}
