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
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.geeksville.info.Units;
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
		
		fillPosition(R.id.latitude, w.latitude, true);
		fillPosition(R.id.longitude, w.longitude, false);

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
		int degs = (int) ((NumberEdit) group.findViewById(R.id.degree)).getDouble();
		int mins = (int) ((NumberEdit) group.findViewById(R.id.minute)).getDouble();
		int secs = (int) ((NumberEdit) group.findViewById(R.id.second)).getDouble();

		// FIXME, support n/s e/w
		boolean isPositive = isLatitude ? waypoint.latitude >= 0 : waypoint.longitude >= 0;

		return LocationUtils.DMSToDegrees(degs, mins, secs, isPositive);
	}

	/**
	 * Fill a lat or long group text fields with a given position
	 * 
	 * @param groupId
	 * @param degIn
	 * @param isLatitude
	 */
	private void fillPosition(int groupId, double degIn, boolean isLatitude) {
		ViewGroup group = (ViewGroup) layout.findViewById(groupId);

		String[] dms = LocationUtils.degreesToDMS(degIn, isLatitude);

		NumberEdit v = ((NumberEdit) group.findViewById(R.id.degree));
		v.setText(dms[0]);
		v.setIntOnly(true);
		v.setMinMax(0, 60);
		v = ((NumberEdit) group.findViewById(R.id.minute));
		v.setText(dms[1]);
		v.setIntOnly(true);
		v.setMinMax(0, 60);
		v = ((NumberEdit) group.findViewById(R.id.second));
		v.setText(dms[2]);
		v.setIntOnly(true);
		v.setMinMax(0, 60);
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
