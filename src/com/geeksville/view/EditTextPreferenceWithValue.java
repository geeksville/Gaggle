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

import com.geeksville.gaggle.R;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

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
