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

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geeksville.android.LifeCycleHandler;
import com.geeksville.android.LifeCyclePublisher;
import com.geeksville.gaggle.R;

/**
 * A view containing a single InfoField
 * 
 * @author kevinh
 * 
 *         We support different layouts by passing in a id_layout attribute. We
 *         look for the following subviews: infodock_label: a full label
 *         infodock_shortlabel: a short label (suitable for map) infodock_units:
 *         units display
 */
public class InfoDock extends LinearLayout implements
    InfoField.OnChangedListener, LifeCycleHandler {

  static final String TAG = "InfoDock";

  /**
   * The layout we are using for this doc
   */
  int layoutId;

  InfoField contents = null;

  TextView shortLabel;

  TextView label, text, units;

  ImageView image;

  // Need handler for callbacks to the UI thread
  private final Handler handler = new Handler();

  /**
   * Used to check for 'real' changes of text values
   */
  private String oldText;

  /**
   * Have we already inflated this component?
   */
  private boolean isInflated = false;

  private int defaultTextColor;

  /**
   * Constructor
   * 
   * @param context
   * @param layoutId
   *          An ID such as R.layout.info_dock_wide
   * @param fieldName
   */
  public InfoDock(Context context, int layoutId, String fieldName) {
    super(context);

    listenToLifecycle();

    this.layoutId = layoutId;
    setInfoField(fieldName);
  }

  public InfoDock(Context context, AttributeSet attrs) {
    super(context, attrs);

    listenToLifecycle();

    TypedArray arr = context
        .obtainStyledAttributes(attrs, R.styleable.InfoDock);

    // The user probably wants to specify a field name
    String fieldName = arr.getString(R.styleable.InfoDock_info_field);

    // default to a wide layout unless the user asked for something else
    layoutId = arr.getResourceId(R.styleable.InfoDock_layout_id,
        R.layout.info_dock_wide);
    arr.recycle();

    if (fieldName != null)
      setInfoField(fieldName);
  }

  /**
   * When our app is paused, we want to stop updating our widgets
   */
  void listenToLifecycle() {
    if (getContext() instanceof LifeCyclePublisher)
      ((LifeCyclePublisher) getContext()).addLifeCycleHandler(this);
  }

  /**
   * Add the children from our layout.xml - FIXME, is there a better way to do
   * this?
   */
  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    Context context = getContext();
    ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
        .inflate(layoutId, this);

    label = (TextView) findViewById(R.id.infodock_label);
    shortLabel = (TextView) findViewById(R.id.infodock_shortlabel);
    text = (TextView) findViewById(R.id.infodock_text);
    units = (TextView) findViewById(R.id.infodock_units);
    image = (ImageView) findViewById(R.id.infodock_image);

    defaultTextColor = text.getTextColors().getDefaultColor();

    // If we already have contents, fill fields and invalidate as needed
    setContents(contents);

    isInflated = true;

    setSaveEnabled(true);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.geeksville.android.LifeCycleHandler#onPause()
   */
  @Override
  public void onPause() {
    if (contents != null)
      contents.onHidden();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.geeksville.android.LifeCycleHandler#onResume()
   */
  @Override
  public void onResume() {
    if (contents != null) {
      contents.onShown();

      updateLabels();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.geeksville.android.LifeCycleHandler#onStart()
   */
  @Override
  public void onStart() {
    // nothing
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.geeksville.android.LifeCycleHandler#onStop()
   */
  @Override
  public void onStop() {
    // nothing
  }

  // We keep a cache of all infofields, so we can keep reusing them
  static Map<String, InfoField> infoFields = new HashMap<String, InfoField>();

  @SuppressWarnings("unchecked")
  public void setInfoField(String fieldName) {
    InfoField f;

    try {
      if (!infoFields.containsKey(fieldName)) {
        Class c = Class.forName(fieldName);
        Constructor<InfoField> cons = c.getConstructor((Class[]) null);

        f = cons.newInstance();

        // if onCreate fails, we still leave our dock around but
        // disabled
        try {
          // Pass in null if we are running in eclipse (by noticing
          // the
          // context is not Activity)
          Activity activity = (getContext() == null) ? null : Activity.class
              .isInstance(getContext()) ? (Activity) getContext() : null;
          f.onCreate(activity);

        } catch (Throwable ex) {
          // We catch Throwable instead of Exception because VerifyErrors can
          // occur on android 1.5

          // If we failed to create the info field the user probably
          // doesn't have the hardware on their phone
          Log.e(TAG,
              "Can't create info dock for " + fieldName + " " + ex.getMessage());
          setEnabled(false);
        }

        infoFields.put(fieldName, f);
      } else
        f = infoFields.get(fieldName);

      // If we are already inflated, we'll need to swap info fields now
      if (isInflated)
        setContents(f);
      else
        contents = f;
    } catch (Exception ex) {
      throw new RuntimeException("Can't create InfoField", ex); // Should
      // not
      // happen
      // post
      // development
    }
  }

  /**
   * Update our units and short label
   */
  private void updateLabels() {
    if (label != null)
      label.setText(contents.getLabel());

    if (shortLabel != null)
      shortLabel.setText(contents.getShortLabel());

    // We might not be displaying the units field
    if (units != null)
      units.setText(contents.getUnits());
  }

  /**
   * Install a new info field into this dock
   * 
   * @param f
   */
  private void setContents(InfoField f) {
    // Tell the old field that it wasn't being shown no more
    // onVisibilityChanged(false);

    // We no longer care about our old contents
    if (contents != null) {
      contents.setOnChanged(null);
      contents.onHidden();
    }

    contents = f;

    if (contents != null) {
      contents.onShown();

      updateLabels();

      // Subscribe to this info field
      contents.setOnChanged(this);

      // Draw the adjustable contents of the control now, so as to prevent
      // flicker
      drawInfoContents();
    }
  }

  @Override
  public void onInfoChanged(InfoField source) {
    // Only update the GUI if we must
    String newText = contents.getText();

    // If the user is using an image, we are not yet smart enough to optize
    // that case - just let the call happen
    if (!newText.equals(oldText) || image != null) {
      oldText = newText;

      handler.post(infoChangedGuiWork);
    }
  }

  private void drawInfoContents() {
    int color = contents.getTextColor();
    text.setTextColor(color != -1 ? color : defaultTextColor);

    text.setText(oldText);

    if (image != null) {
      Drawable img = contents.getImage();

      if (img != null) {
        image.setImageDrawable(img);
        image.setVisibility(VISIBLE);
        image.invalidate();
      } else {
        // If this info field doesn't have a drawable now, no point
        // in drawing the image view
        image.setVisibility(INVISIBLE);
      }
    }
    // Redraw our drawable too
    // if (image != null && image.getVisibility() == VISIBLE)
    // image.invalidate();
  }

  /**
   * Handles updates from the infofield, but guaranteed to run in the gui thread
   */
  final Runnable infoChangedGuiWork = new Runnable() {
    public void run() {
      drawInfoContents();
    }
  };

}
