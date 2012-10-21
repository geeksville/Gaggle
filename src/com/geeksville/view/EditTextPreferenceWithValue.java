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
package com.geeksville.view;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.geeksville.gaggle.R;

/**
 * Display current prefs value on the preferences menu itself
 * 
 * @author kevinh
 * 
 * @license From http://www.androidsnippets.org/about/tos/ That you grant any
 *          third party who sees the code you post a royalty-free, non-exclusive
 *          license to copy and distribute that code and to make and distribute
 *          derivative works based on that code. You may include license terms
 *          in snippets you post, if you wish to use a particular license (such
 *          as the BSD license or GNU GPL), but that license must permit
 *          royalty-free copying, distribution and modification of the code to
 *          which it is applied.
 * 
 *          From: http://www.androidsnippets.org/snippets/34/
 */
public class EditTextPreferenceWithValue extends EditTextPreference {

	private TextView mValueText;

	public EditTextPreferenceWithValue(Context context, AttributeSet attrs) {
		super(context, attrs);
		setLayoutResource(R.layout.preference_with_value);
	}

	public EditTextPreferenceWithValue(Context context) {
		super(context);
		setLayoutResource(R.layout.preference_with_value);
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		mValueText = (TextView) view.findViewById(R.id.preference_value);
		if (mValueText != null) {
			mValueText.setText(getText());
		}
	}

	@Override
	public void setText(String text) {
		super.setText(text);
		if (mValueText != null) {
			mValueText.setText(getText());
		}
	}

}
