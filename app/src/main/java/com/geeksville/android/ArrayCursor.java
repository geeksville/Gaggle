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

import java.util.ArrayList;

/**
 * A cursor that pulls its data from a set of ArrayLists
 * 
 * @author khester
 * 
 */
public class ArrayCursor extends InMemoryCursor {

	@SuppressWarnings("unchecked")
	ArrayList[] colVals;

	@SuppressWarnings("unchecked")
	public ArrayCursor(String[] colNames, ArrayList[] colVals) {
		super(colNames);

		this.colVals = colVals;
	}

	@Override
	protected boolean updateCurRow(int oldPosition, int newPosition, Object[] curRow) {

		for (int col = 0; col < colVals.length; col++)
			curRow[col] = colVals[col].get(newPosition);

		return true;
	}

	@Override
	public int getCount() {
		return colVals[0].size();
	}

}
