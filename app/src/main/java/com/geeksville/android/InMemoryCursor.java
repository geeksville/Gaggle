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
package com.geeksville.android;

import android.database.AbstractCursor;

/**
 * A cursor that is designed to be filled procedurally from updateCurRow
 * 
 * @author kevinh
 * 
 */
public abstract class InMemoryCursor extends AbstractCursor {

	private String[] colNames;

	private Object[] curRow;

	protected InMemoryCursor(String[] colNames) {
		this.colNames = colNames;

		curRow = new Object[colNames.length];
	}

	/**
	 * @see android.database.AbstractCursor#onMove(int, int)
	 */
	@Override
	public boolean onMove(int oldPosition, int newPosition) {
		return updateCurRow(oldPosition, newPosition, curRow);
	}

	/**
	 * Subclass must provide code to fill in the current row data
	 * 
	 * @param oldPosition
	 * @param newPosition
	 * @param curRow
	 * @return true if the row data is now valid
	 */
	protected abstract boolean updateCurRow(int oldPosition, int newPosition, Object[] curRow);

	@Override
	public String[] getColumnNames() {
		// TODO Auto-generated method stub
		return colNames;
	}

	@Override
	public double getDouble(int column) {
		return (Double) curRow[column];
	}

	@Override
	public float getFloat(int column) {
		return (Float) curRow[column];
	}

	@Override
	public int getInt(int column) {
		return (Integer) curRow[column];
	}

	@Override
	public long getLong(int column) {
		return (Long) curRow[column];
	}

	@Override
	public short getShort(int column) {
		return (Short) curRow[column];
	}

	@Override
	public String getString(int column) {
		Object o = curRow[column];
		return (o == null) ? null : o.toString();
	}

	@Override
	public boolean isNull(int column) {
		return curRow[column] == null;
	}

}
