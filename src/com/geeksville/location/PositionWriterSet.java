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
			int bearing, float groundSpeed, float[] accel) {
		for (PositionWriter w : writers) {
			w.emitPosition(time, latitude, longitude, altitude, bearing, groundSpeed, accel);
		}
	}

	@Override
	public void emitProlog() {
		for (PositionWriter w : writers) {
			w.emitProlog();
		}
	}

}
