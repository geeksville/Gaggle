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
package com.geeksville.gaggle;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.geeksville.info.Units;
import com.geeksville.info.Units.CoordinateSet;
import com.geeksville.location.LocationUtils;
import com.geeksville.location.Waypoint;
import com.geeksville.view.EnhancedSpinner;
import com.geeksville.view.NumberEdit;

/**
 * FIXME - add support for showing fractional minutes instead of DMS
 * 
 * @author kevinh
 * 
 */
public class WaypointDialog implements DialogInterface.OnClickListener {

	private Waypoint waypoint;
	private View layout;
	private EditText name;
	private EditText description;
	private AlertDialog alertDialog;
	private NumberEdit altitudeView;
	private Runnable onOkay, onGoto;
	private EnhancedSpinner typeSpinner;

	public WaypointDialog(Context c, Waypoint w, final Runnable onOkay, Runnable onGoto) {

		waypoint = w;
		this.onOkay = onOkay;
		this.onGoto = onGoto;

		LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layout = inflater.inflate(R.layout.waypoint_edit, null);

		name = (EditText) layout.findViewById(R.id.name);
		description = (EditText) layout.findViewById(R.id.description);
		
		typeSpinner = (EnhancedSpinner) layout.findViewById(R.id.type);
		typeSpinner.setSelection(w.type.ordinal());

		name.setText(w.name);
		description.setText(w.description);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
		CoordinateSet coordinatesset = CoordinateSet.valueOf(prefs.getString("coordinateset_pref", "DMS"));
		
		fillPosition(R.id.latitude, w.latitude, true, coordinatesset);
		fillPosition(R.id.longitude, w.longitude, false, coordinatesset);

		altitudeView = ((NumberEdit) layout.findViewById(R.id.altitude));
		altitudeView.setText(Units.instance.metersToAltitude(w.altitude));
		altitudeView.setIntOnly(true);

		TextView altUnits = (TextView) layout.findViewById(R.id.altitude_units);
		altUnits.setText(Units.instance.getAltitudeUnits());

		AlertDialog.Builder builder = new AlertDialog.Builder(c);
		builder.setTitle(R.string.edit_waypoint);
		builder.setView(layout);
		builder.setCancelable(true);
		builder.setPositiveButton(R.string.okay, this);
		builder.setNeutralButton(R.string.go_to, this);
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});

		alertDialog = builder.create();
	}


	public void show() {
		alertDialog.show();
	}

	/**
	 * Read text fields to generate a latitude
	 * 
	 * @param groupId
	 * @param isLatitude
	 * @return
	 */
	private double readPosition(int groupId, boolean isLatitude) throws NumberFormatException {
		ViewGroup group = (ViewGroup) layout.findViewById(groupId);

		// FIXME, also validate ranges on params
		double degs = ((NumberEdit) group.findViewById(R.id.degree)).getDouble();
		double mins = ((NumberEdit) group.findViewById(R.id.minute)).getDouble();
		double secs = ((NumberEdit) group.findViewById(R.id.second)).getDouble();

		boolean isPositive = "NE".contains(((Spinner) group.findViewById(R.id.hemisphere)).getSelectedItem().toString());

		return LocationUtils.DMSToDegrees(degs, mins, secs, isPositive);
	}

	/**
	 * Fill a lat or long group text fields with a given position
	 * 
	 * @param groupId
	 * @param degIn
	 * @param isLatitude 
	 */
	private void fillPosition(int groupId, double degIn, boolean isLatitude, CoordinateSet coordinateset) {

		ViewGroup group = (ViewGroup) layout.findViewById(groupId);
		
		String[] dms = null;
		NumberEdit vd = ((NumberEdit) group.findViewById(R.id.degree));
		NumberEdit vm = ((NumberEdit) group.findViewById(R.id.minute));
		NumberEdit vs = ((NumberEdit) group.findViewById(R.id.second));
		ViewGroup.LayoutParams params;
		dms = 	(coordinateset == CoordinateSet.DMS) ? LocationUtils.degreesToDMS(degIn, isLatitude) : 
				(coordinateset == CoordinateSet.DM) ? LocationUtils.degreesToDM(degIn, isLatitude) :
				LocationUtils.degreesToD(degIn, isLatitude);
		switch(coordinateset){
		case DMS:
			vs.setIntOnly(false);
			break;
		case DM:
			vm.setIntOnly(false);
			vm.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
			params = vm.getLayoutParams();
			params.width = 120;			
			vm.setLayoutParams(params);
			vm.setIntOnly(false);
			vs.setVisibility(View.GONE);			
			group.findViewById(R.id.symbol_second).setVisibility(View.GONE);
			break;
		case D:
			vd.setIntOnly(false);
			vd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(9)});
			params = vd.getLayoutParams();
			params.width = 160;			
			vd.setLayoutParams(params);
			vs.setVisibility(View.GONE);
			vm.setVisibility(View.GONE);
			group.findViewById(R.id.symbol_second).setVisibility(View.GONE);
			group.findViewById(R.id.symbol_minute).setVisibility(View.GONE);
			break;
		}
		vd.setMinMax(0, isLatitude ? 90 : 180);
		vd.setText(dms[0]);
		vm.setMinMax(0,60);
		vm.setText(dms[1]);
		vs.setMinMax(0,60);
		vs.setText(dms[2]);	
		Spinner NSEW = (Spinner) group.findViewById(R.id.hemisphere);
		if ("NE".contains(dms[3]))
			NSEW.setSelection(0);
		else
			NSEW.setSelection(1);		
	}

	/**
	 * Handle the okay or goto buttons
	 */
	@Override
	public void onClick(DialogInterface dialog, int which) {
		double latitude = readPosition(R.id.latitude, true);
		double longitude = readPosition(R.id.longitude, false);
		int alt = (int) altitudeView.getDouble();

		// FIXME, validate names for uniqueness
		waypoint.name = name.getText().toString().trim();
		waypoint.description = description.getText().toString().trim();
		waypoint.altitude = (int) Units.instance.altitudeToMeters(alt);
		waypoint.latitude = latitude;
		waypoint.longitude = longitude;
		waypoint.type = Waypoint.Type.values()[typeSpinner.getSelectedItemPosition()];

		// Write our state back to the DB
		if (which == AlertDialog.BUTTON_POSITIVE)
			onOkay.run();
		else if (which == AlertDialog.BUTTON_NEUTRAL)
			onGoto.run();
	}
}
