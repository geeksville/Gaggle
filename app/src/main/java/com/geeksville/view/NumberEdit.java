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
package com.geeksville.view;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * A text field that accepts only doubles or ints, with a specified min/max
 * range
 * 
 * @author kevinh FIXME - validate range
 */
public class NumberEdit extends EditText {

	public NumberEdit(Context context, AttributeSet attrs) {
		super(context, attrs);

		setIntOnly(true); // default to int mode
	}

	private double min, max;

	public void setMinMax(double min, double max) {
		this.min = min;
		this.max = max;
	}

	public void setIntOnly(boolean onlyInt) {
		setInputType(InputType.TYPE_CLASS_NUMBER | (onlyInt ? 0 : InputType.TYPE_NUMBER_FLAG_DECIMAL));
	}

	public double getDouble() {
		// FIXME, validate fields for acceptability
		return Double.parseDouble(this.getText().toString());
	}

	public void setDouble(double d) {
		setText(Double.toString(d));
	}
}
