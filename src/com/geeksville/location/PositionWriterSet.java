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
 * Given a list of position writers, broadcast all updates to all of the
 * writers.
 * 
 * @author kevinh
 * 
 */
public class PositionWriterSet implements PositionWriter {

	PositionWriter[] writers;

	/**
	 * Constructor
	 * 
	 * @param writers
	 */
	public PositionWriterSet(PositionWriter[] writers) {
		this.writers = writers;
	}

	@Override
	public void emitEpilog() {
		for (PositionWriter w : writers) {
			w.emitEpilog();
		}
	}

	@Override
	public void emitPosition(long time, double latitude, double longitude, float altitude,
			int bearing, float groundSpeed, float[] accel, float vspd) {
		for (PositionWriter w : writers) {
			w.emitPosition(time, latitude, longitude, altitude, bearing, groundSpeed, accel, Float.NaN);
		}
	}

	@Override
	public void emitProlog() {
		for (PositionWriter w : writers) {
			w.emitProlog();
		}
	}

}
