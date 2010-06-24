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
package com.geeksville.info;

import android.app.Activity;
import android.graphics.drawable.Drawable;

import com.geeksville.gaggle.R;

/**
 * The base class for little info widgets that can be placed in various
 * locations inside Gaggle
 * 
 * @author kevinh
 * 
 *         I'm not sure if I should instead be making these widgets Views or
 *         Drawables instead. However, both of those items seem too heavyweight
 *         in my initial impression. The idea is that these widgets live inside
 *         of an InfoFieldView. Users can long-click on those InfoDock to switch
 *         out info fields.
 * 
 *         An info field can return its information as a drawable and/or a
 *         string. It should also have a short label string and a much longer
 *         description string (for menus/self documenting UI)
 * 
 *         InfoDock is a linear layout(?) that contains a series of InfoFields
 *         For each info field we programatically make either a TextView or a
 *         ImageView. For each one we will optionally also include the label
 */
public abstract class InfoField {

	private OnChangedListener listener;

	protected Activity context;

	private static int textColor = 0xffffff;

	/**
	 * Used with setOnChanged to find out about changes in this info field
	 * 
	 * @author kevinh
	 * 
	 */
	public interface OnChangedListener {
		/**
		 * Called when our info field has changed contents
		 * 
		 * @param source
		 */
		public void onInfoChanged(InfoField source);
	}

	/**
	 * Used to get the application context to find globals etc...
	 * 
	 * @param context
	 *            null if running inside eclipse
	 */
	public void onCreate(Activity context) {
		this.context = context;

		// The following check is necessary to work in the IDE (FIXME - remove
		// in onStop?
		if (context != null) {
			textColor = context.getResources().getColor(R.color.info_field_text);
		}
	}

	/**
	 * Subclasses should override if they have text contents
	 * 
	 * @return
	 */
	public String getText() {
		return "";
	}

	/**
	 * Return the android id of the color for this text field
	 * 
	 * @return a default color
	 */
	public int getTextColor() {
		return textColor;
	}

	/**
	 * A units suffix that may be displayed at the end of any text display
	 * (likely in a small font)
	 * 
	 * @return
	 */
	public String getUnits() {
		return "";
	}

	/**
	 * Subclasses must override
	 * 
	 * @return
	 */
	public abstract String getLabel();

	/**
	 * A very short label string. Subclasses may override
	 * 
	 * @return
	 */
	public String getShortLabel() {
		return getLabel();
	}

	/**
	 * Get the image to be shown by this info field
	 * 
	 * @return null for no images supported
	 * 
	 *         Note: the dock will only call this method once, we presume that
	 *         if we redraw the ImageView that will be containing this drawable
	 *         that the drawable will do the right thing
	 */
	public Drawable getImage() {
		return null;
	}

	/**
	 * Called when this infofield is now visible (somewhere) Used to start
	 * listening to GPS etc...
	 */
	void onShown() {

	}

	/**
	 * Called when this infofield is now invisible (everywhere) Used to stop
	 * listening to GPS etc...
	 */
	void onHidden() {

	}

	/**
	 * The GUI wants to know when we change
	 * 
	 * @param listener
	 */
	public void setOnChanged(OnChangedListener listener) {
		// fixme-when set to null unsubscribe from our info sources
		// also set to null when our containing infodock gets destroyed
		// have the dock be the lifecycle listener, add onVisible() and
		// onHidden()

		this.listener = listener;

		// Send any updates to get our new container in sync
		onChanged();
	}

	/**
	 * Call this to tell the GUI we have new info
	 */
	protected void onChanged() {
		if (listener != null)
			listener.onInfoChanged(this);
	}

}
