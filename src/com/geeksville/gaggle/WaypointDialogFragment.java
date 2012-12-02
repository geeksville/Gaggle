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
package com.geeksville.gaggle;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
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
public class WaypointDialogFragment extends DialogFragment implements
		DialogInterface.OnClickListener {

	private Waypoint waypoint;
	private View layout;
	private EditText name;
	private EditText description;
	private AlertDialog alertDialog;
	private NumberEdit altitudeView;
	private Runnable onOkay, onGoto;
	private EnhancedSpinner typeSpinner;
	private Context context;
	private boolean create;

	public WaypointDialogFragment(Context c, Waypoint w, final Runnable onOkay, boolean create) {
		this(c, w, onOkay, null, create);
	}

	public WaypointDialogFragment(Context c, Waypoint w, final Runnable onOkay,
			Runnable onGoto, boolean create) {

		this.waypoint = w;
		this.onOkay = onOkay;
		this.onGoto = onGoto;
		this.context = c;
		this.create = create;
	}

	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layout = inflater.inflate(R.layout.waypoint_edit, null);

		name = (EditText) layout.findViewById(R.id.name);
		description = (EditText) layout.findViewById(R.id.description);

		typeSpinner = (EnhancedSpinner) layout.findViewById(R.id.type);
		typeSpinner.setSelection(waypoint.type.ordinal());

		if(create)
			name.setHint(waypoint.name);
		else
			name.setText(waypoint.name);
		description.setText(waypoint.description);

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		CoordinateSet coordinatesset = CoordinateSet.valueOf(prefs.getString(
				"coordinateset_pref", "DMS"));

		fillPosition(R.id.latitude, waypoint.latitude, true, coordinatesset);
		fillPosition(R.id.longitude, waypoint.longitude, false, coordinatesset);

		altitudeView = ((NumberEdit) layout.findViewById(R.id.altitude));
		altitudeView
				.setText(Units.instance.metersToAltitude(waypoint.altitude));
		altitudeView.setIntOnly(true);

		TextView altUnits = (TextView) layout.findViewById(R.id.altitude_units);
		altUnits.setText(Units.instance.getAltitudeUnits());

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		if(create)
			builder.setTitle(context.getString(R.string.create_waypoint));
		else
			builder.setTitle(context.getString(R.string.edit_waypoint));
		builder.setView(layout);
		builder.setCancelable(true);
		builder.setPositiveButton(R.string.okay, this);
		if (onGoto != null) {
			builder.setNeutralButton(R.string.go_to, this);
		}
		builder.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});

		alertDialog = builder.create();

		return alertDialog;
	}

	// this remains, so the old way does not need to be refactorered
	// immedaitely. Ultimately the real dialog creation
	// needs to happen in oncreateDialog, and not in the contructor.
//	public void show() {
//		alertDialog.show();
//	}

	/**
	 * Read text fields to generate a latitude
	 * 
	 * @param groupId
	 * @param isLatitude
	 * @return
	 */
	private double readPosition(int groupId, boolean isLatitude)
			throws NumberFormatException {
		ViewGroup group = (ViewGroup) layout.findViewById(groupId);

		// FIXME, also validate ranges on params
		double degs = ((NumberEdit) group.findViewById(R.id.degree))
				.getDouble();
		double mins = ((NumberEdit) group.findViewById(R.id.minute))
				.getDouble();
		double secs = ((NumberEdit) group.findViewById(R.id.second))
				.getDouble();

		boolean isPositive = "NE".contains(((Spinner) group
				.findViewById(R.id.hemisphere)).getSelectedItem().toString());

		return LocationUtils.DMSToDegrees(degs, mins, secs, isPositive);
	}

	/**
	 * Fill a lat or long group text fields with a given position
	 * 
	 * @param groupId
	 * @param degIn
	 * @param isLatitude
	 */
	private void fillPosition(int groupId, double degIn, boolean isLatitude,
			CoordinateSet coordinateset) {

		ViewGroup group = (ViewGroup) layout.findViewById(groupId);

		String[] dms = null;
		NumberEdit vd = ((NumberEdit) group.findViewById(R.id.degree));
		NumberEdit vm = ((NumberEdit) group.findViewById(R.id.minute));
		NumberEdit vs = ((NumberEdit) group.findViewById(R.id.second));
		ViewGroup.LayoutParams params;
		dms = (coordinateset == CoordinateSet.DMS) ? LocationUtils
				.degreesToDMS(degIn, isLatitude)
				: (coordinateset == CoordinateSet.DM) ? LocationUtils
						.degreesToDM(degIn, isLatitude) : LocationUtils
						.degreesToD(degIn, isLatitude);
		switch (coordinateset) {
		case DMS:
			vs.setIntOnly(false);
			break;
		case DM:
			vm.setIntOnly(false);
			vm.setFilters(new InputFilter[] { new InputFilter.LengthFilter(8) });
			params = vm.getLayoutParams();
			params.width = 120;
			vm.setLayoutParams(params);
			vm.setIntOnly(false);
			vs.setVisibility(View.GONE);
			group.findViewById(R.id.symbol_second).setVisibility(View.GONE);
			break;
		case D:
			vd.setIntOnly(false);
			vd.setFilters(new InputFilter[] { new InputFilter.LengthFilter(9) });
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
		vm.setMinMax(0, 60);
		vm.setText(dms[1]);
		vs.setMinMax(0, 60);
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
		if (create) {
			waypoint.name = name.getText().toString();
			Editable nameText = name.getText();
			if(nameText ==null || nameText.toString().trim().equals("")){
				waypoint.name = name.getHint().toString();
			} else {
				waypoint.name = name.getText().toString().trim();
			}
		} else {
			waypoint.name = name.getText().toString().trim();
		}
		waypoint.description = description.getText().toString().trim();
		waypoint.altitude = (int) Units.instance.altitudeToMeters(alt);
		waypoint.latitude = latitude;
		waypoint.longitude = longitude;
		waypoint.type = Waypoint.Type.values()[typeSpinner
				.getSelectedItemPosition()];

		// Write our state back to the DB
		if (which == AlertDialog.BUTTON_POSITIVE)
			onOkay.run();
		else if (which == AlertDialog.BUTTON_NEUTRAL)
			onGoto.run();
	}
}
