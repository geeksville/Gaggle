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
package com.geeksville.util;

/**
 * An int array that can grow (without resorting to ArrayList-Integer
 * 
 * @author kevinh
 * 
 */
public class FloatArray {
	float[] array;

	int len = 0;

	public FloatArray(int initialCapacity) {
		array = new float[initialCapacity];
	}

	public FloatArray() {
		this(32);
	}

	/**
	 * Copy from a regular non expandable array
	 * 
	 * @param src
	 */
	public FloatArray(float[] src) {
		array = src.clone();
		len = array.length;
	}

	public void add(float val) {
		if (len == array.length) {
			// Copy and grow
			float[] na = new float[array.length * 2];
			System.arraycopy(array, 0, na, 0, array.length);
			array = na;
		}

		array[len++] = val;
	}

	public float get(int pos) {
		return array[pos];
	}

	public int length() {
		return len;
	}

	/**
	 * Direct access to the array inside
	 * 
	 * @return WARNING: This is low level access, and you may find more points
	 *         in this array than you expect. Use length() to guard access.
	 */
	public float[] toUnsafeArray() {
		return array;
	}

	public float[] toArray() {
		float[] res = new float[length()];
		System.arraycopy(array, 0, res, 0, length());
		return res;
	}
}
