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

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
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
public class InfoDock extends LinearLayout implements InfoField.OnChangedListener, LifeCycleHandler {

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

	/**
	 * Constructor
	 * 
	 * @param context
	 * @param layoutId
	 *            An ID such as R.layout.info_dock_wide
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

		TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.InfoDock);

		// The user probably wants to specify a field name
		String fieldName = arr.getString(R.styleable.InfoDock_info_field);

		// default to a wide layout unless the user asked for something else
		layoutId = arr.getResourceId(R.styleable.InfoDock_layout_id, R.layout.info_dock_wide);
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
		((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				layoutId, this);

		label = (TextView) findViewById(R.id.infodock_label);
		shortLabel = (TextView) findViewById(R.id.infodock_shortlabel);
		text = (TextView) findViewById(R.id.infodock_text);
		units = (TextView) findViewById(R.id.infodock_units);
		image = (ImageView) findViewById(R.id.infodock_image);

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
		if (contents != null)
			contents.onShown();
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
		try {
			InfoField f;

			if (!infoFields.containsKey(fieldName)) {
				Class c = Class.forName(fieldName);
				Constructor<InfoField> cons = c.getConstructor((Class[]) null);

				f = cons.newInstance();

				// Pass in null if we are running in eclipse (by noticing the
				// context is not Activity)
				Activity activity = (getContext() == null) ? null
						: Activity.class.isInstance(getContext()) ? (Activity) getContext()
						: null;
				f.onCreate(activity);

				// This cache doesn't work yet?
				infoFields.put(fieldName, f);
			} else
				f = infoFields.get(fieldName);

			// If we are already inflated, we'll need to swap info fields now
			if (isInflated)
				setContents(f);
			else
				contents = f;

		} catch (Exception ex) {
			throw new RuntimeException("Exception while creating " + fieldName, ex); // Should
			// not
			// happen
			// post
			// development
		}
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

			if (label != null)
				label.setText(contents.getLabel());

			if (shortLabel != null)
				shortLabel.setText(contents.getShortLabel());

			// We might not be displaying the units field
			if (units != null)
				units.setText(contents.getUnits());

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
		text.setTextColor(color);

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
	 * Handles updates from the infofield, but guaranteed to run in the gui
	 * thread
	 */
	final Runnable infoChangedGuiWork = new Runnable() {
		public void run() {
			drawInfoContents();
		}
	};

}
