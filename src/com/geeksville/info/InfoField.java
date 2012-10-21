/****************************************************************************************
 * Gaggle is Copyright 2010, 2011, and 2012 by Kevin Hester of Geeksville Industries LLC 
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

  private static int textColor = -1;

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
   *          null if running inside eclipse
   */
  public void onCreate(Activity context) {
    this.context = context;
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
   * @return a default color (or -1 to use the color specified by the style)
   */
  public int getTextColor() {
    return textColor;
  }

  /**
   * A units suffix that may be displayed at the end of any text display (likely
   * in a small font)
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
   *         Note: the dock will only call this method once, we presume that if
   *         we redraw the ImageView that will be containing this drawable that
   *         the drawable will do the right thing
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
