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
package com.geeksville.info;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.geeksville.gaggle.R;

/**
 * A vertical list of info docks
 * 
 * @author kevinh
 * 
 */
public class InfoListView extends ListView {

  private static final String TAG = "InfoListView";

  private static final String[] classNames = {
      "com.geeksville.info.InfoVerticalSpeed",
      "com.geeksville.info.InfoGlideRatio", "com.geeksville.info.InfoAltitude",
      "com.geeksville.info.InfoGroundSpeed",
      "com.geeksville.info.InfoDistWaypoint",
      "com.geeksville.info.InfoGRWaypoint",
      "com.geeksville.info.InfoNearestLZ",
      "com.geeksville.info.InfoGRNearestLZ", "com.geeksville.info.InfoCompass",
      "com.geeksville.info.InfoLatitude", "com.geeksville.info.InfoLongitude",
      "com.geeksville.info.InfoGMeter", "com.geeksville.info.InfoBarometer", };

  private ArrayList<String> shownNames = new ArrayList<String>();

  private Set<String> checkedNames = new HashSet<String>();

  private CheckedDockAdapter adapter;

  private int rowLayoutId;

  public InfoListView(Context context, AttributeSet attrs) {
    super(context, attrs);

    TypedArray arr = context.obtainStyledAttributes(attrs,
        R.styleable.InfoListView);

    // default to a wide layout unless the user asked for something else
    rowLayoutId = arr.getResourceId(R.styleable.InfoListView_row_layout_id,
        R.layout.infodock_list_row);

    setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    setAdapter(adapter = new CheckedDockAdapter(context));

    // Unless we know otherwise assume we want all selected
    for (String s : classNames)
      checkedNames.add(s);

    setShowCheckmarks(false);
    setShowUncheckedRows(false);

    setFocusable(false);
  }

  public String[] getChecked() {
    String[] result = new String[checkedNames.size()];

    result = checkedNames.toArray(result);

    return result;
  }

  public void setChecked(String[] checked) {
    checkedNames.clear();
    for (String s : checked)
      checkedNames.add(s);

    updateShownNames();
  }

  void setShowCheckmarks(boolean showCheckmarks) {
    this.showCheckmarks = showCheckmarks;
    requestLayout();
  }

  boolean isShowCheckmarks() {
    return showCheckmarks;
  }

  private void updateShownNames() {
    shownNames.clear();
    for (String name : classNames)
      // We do it this way because we want to
      // preserve ordering
      if (showUncheckedRows || checkedNames.contains(name))
        shownNames.add(name);

    adapter.notifyDataSetChanged();
  }

  void setShowUncheckedRows(boolean showUncheckedRows) {
    this.showUncheckedRows = showUncheckedRows;

    updateShownNames();
  }

  boolean isShowUncheckedRows() {
    return showUncheckedRows;
  }

  private boolean showCheckmarks = false;

  private boolean showUncheckedRows = false;

  /**
   * generate an optionally checkable set of info docks
   * 
   * @author kevinh
   * 
   */
  private class CheckedDockAdapter extends BaseAdapter implements
      CompoundButton.OnCheckedChangeListener {
    LayoutInflater inflater;

    public CheckedDockAdapter(Context context) {
      inflater = (LayoutInflater) context
          .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Click the checkbox for included items, and place the infofields in the
     * docks
     * 
     * @see android.widget.ArrayAdapter#getView(int, android.view.View,
     *      android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

      // Let's reuse old rows if we can
      View row;
      if (convertView != null)
        row = convertView;
      else
        row = inflater.inflate(rowLayoutId, null);

      // Put the field into our dock, but work in the IDE
      try {
        InfoDock dock = (InfoDock) row.findViewById(R.id.info);
        String name = (String) getItem(position);
        boolean isChecked = checkedNames.contains(name);

        dock.setInfoField(name);

        boolean isValid = dock.isEnabled(); // false if we couldn't make
        // the info field
        if (!isValid)
          checkedNames.remove(name);
        CheckBox checkbox = (CheckBox) row.findViewById(R.id.checkbox);
        checkbox.setEnabled(isValid);
        checkbox.setChecked(isValid && isChecked);
        checkbox.setVisibility(isShowCheckmarks() ? VISIBLE : GONE);
        checkbox.setTag(name);
        checkbox.setOnCheckedChangeListener(this);

      } catch (ClassCastException ex) {
        Log.w(TAG, "Ignoring " + ex.getMessage());
      }

      return row;
    }

    @Override
    public int getCount() {
      return shownNames.size();
    }

    @Override
    public Object getItem(int position) {
      return shownNames.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
      String name = (String) buttonView.getTag();

      if (isChecked)
        checkedNames.add(name);
      else
        checkedNames.remove(name);
    }

  }
}
