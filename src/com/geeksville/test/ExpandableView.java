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
package com.geeksville.test;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.*;

/**
 * A compound view that has a small button to open or close the bottom portion of the view.
 * 
 * @author kevinh
 * 
 * The button for open/closing the view will be in the bottom right (FIXME set via property)
 * of the top view.
 * 
 * I keep the children layout the same as a regular LinearLayout, so that the developer can
 * use the regular Android Xml editor.
 */
public class ExpandableView extends LinearLayout {

	private boolean didSpecialFixup = false;
	
	private ImageButton expandButton;
	
	private boolean isExpanded = false;
	
	private View primaryView;
	
	public ExpandableView(Context context) {
		super(context);
	}
	
	
	public ExpandableView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	

	
	/**
	 * Toggle expanded state based on the user's clicks
	 */
	private OnClickListener expandButtonListener = new OnClickListener() {
	    public void onClick(View v) {
	      setExpanded(!isExpanded);
	    }
	};

	void addSpecialViews() {
		
		// We need at least two children to do anything special
		if(didSpecialFixup || getChildCount() < 2)
			return;
		
		didSpecialFixup = true;
		
		// FIXME - create this button later, but for now the fact that this thing
		// throws is my crufty way of detecting I'm running inside of ADT/eclipse and I
		// should act like a linear layout.  I need a better way to detect not being on a
		// device/simulator
		try {
		Context context = getContext();	
		expandButton = new ImageButton(context);
		}
		catch(NullPointerException ex) {
			return;
		}
		
		// Reparent the first view into a horizontal linear view
		// with the turny button
		primaryView = getChildAt(0);
		
		removeView(primaryView);
		LinearLayout primaryWrapper = new LinearLayout(getContext());
		primaryWrapper.setOrientation(HORIZONTAL);
		primaryWrapper.addView(primaryView);
		
		// Now add our button - we want something like this
		/* <ImageButton android:src="@android:drawable/arrow_down_float"
				android:layout_height="wrap_content" android:layout_width="wrap_content"
				 android:id="@+id/ImageView01" android:layout_gravity="bottom" android:background="@android:color/transparent"/>  */
		
		expandButton.setBackgroundResource(android.R.color.transparent); 
		expandButton.setOnClickListener(expandButtonListener);

		LayoutParams buttonLayout = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		primaryWrapper.addView(expandButton, buttonLayout);
		
		addView(primaryWrapper, 0);
		
		// Set initial expanded state
		expandChildren(isExpanded);
	}
	
    /**
     * We intercept onMeasure to find out when layout is starting
     * 
     * @see android.view.View#measure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	addSpecialViews();
    	
    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
	
	/**
	 * Expand or contract our children as appropriate
	 * @param expand
	 */
	private void expandChildren(boolean expand) {
		
		// Update our button to show the correct image
		// FIXME, use a better image, and make its selection more customizable
		expandButton.setImageResource(expand 
				? android.R.drawable.arrow_down_float 
				: android.R.drawable.arrow_up_float);
		
		
		int numchild = getChildCount();
		
		// We leave the first element alone
		for(int i = 1; i < numchild; i++) {
			View v = getChildAt(i);
			
			v.setVisibility(expand ? VISIBLE : GONE);
		}
	}
	
	/**
	 * Open or close the expanded portion of our view
	 * @param isExpand
	 */
	public void setExpanded(boolean isExpand) {
		if(isExpand != isExpanded) {
			isExpanded = isExpand;
			
			// If we are hiding our subview, make sure to not leave the focus on any of the views inside it
			if(!isExpanded)
				primaryView.requestFocus();
			
			expandChildren(isExpanded);
			requestLayout();
		}
	}
}
