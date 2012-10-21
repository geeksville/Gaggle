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

import java.util.Date;

import com.geeksville.info.FlightSummary;
import com.geeksville.info.Units;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SummaryListActivity extends ListActivity {
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		displayData();
	}
	
	private void displayData() {
		Bundle summaryBundle = getIntent().getExtras();
		FlightSummary summary = new FlightSummary(summaryBundle);
		
		Units units = Units.instance;
		String altUnits = units.getAltitudeUnits();
		String distUnits = units.getDistanceUnits();
		String speedUnits = units.getSpeedUnits();
			
		String startDate = summary.getStartDate().toLocaleString();
		String endDate = summary.getEndDate().toLocaleString();
		String duration = getFriendlyTimeDifference(summary.getStartDate(), summary.getEndDate());
		String startAltitude = units.metersToAltitude(summary.getStartAltitude()) + altUnits;
		String maxAltitudeAfterLaunch = units.metersToAltitude(summary.getMaxAltitudeAfterLaunch()) + altUnits;
		String maxGroundSpeed = units.kilometerPerHourToSpeed(summary.getMaxGroundSpeed()) + speedUnits;
		String averageGroundSpeed = units.kilometerPerHourToSpeed(summary.getAverageGroundSpeed()) + speedUnits;
		String totalGroundDistance = units.metersToDistance(summary.getTotalGroundDistance()) + distUnits;
		String totalVerticalDistance = units.metersToAltitude(summary.getTotalVerticalDistance()) + altUnits;
		String maxDistanceFromLaunch = units.metersToDistance(summary.getMaxDistanceFromLaunch()) + distUnits;
		
		String[] labels = new String[] { 
				getString(R.string.start), 
				getString(R.string.end), 
				getString(R.string.duration), 
				getString(R.string.start_altitude), 
				getString(R.string.max_altitude_after_launch),
				getString(R.string.max_ground_speed),
				getString(R.string.average_ground_speed),
				getString(R.string.total_ground_distance_traveled),
				getString(R.string.total_vertical_distance_traveled),
				getString(R.string.max_distance_from_launch)
		};
		
		String[] values = new String[] { 
				startDate, 
				endDate, 
				duration, 
				startAltitude, 
				maxAltitudeAfterLaunch,
				maxGroundSpeed,
				averageGroundSpeed,
				totalGroundDistance,
				totalVerticalDistance,
				maxDistanceFromLaunch
		};
		
		setListAdapter(new SummaryAdapter(this, labels, values));
	}
	
	private class SummaryAdapter extends ArrayAdapter<String> {
		private final Activity context;
		private final String[] labels;
		private final String[] values;

		public SummaryAdapter(Activity context, String[] labels, String[] values) {
			super(context, R.layout.summary_main, labels);
			this.context = context;
			this.labels = labels;
			this.values = values;
		}
		
		private class ViewHolder {
			public TextView labelView;
			public TextView valueView;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			View rowView = convertView;
			if (rowView == null) {
				LayoutInflater inflater = context.getLayoutInflater();
				rowView = inflater.inflate(R.layout.summary_main, null, true);
				holder = new ViewHolder();
				holder.labelView = (TextView) rowView.findViewById(R.id.dataLabel);
				holder.valueView = (TextView) rowView.findViewById(R.id.dataValue);
				rowView.setTag(holder);
			} else {
				holder = (ViewHolder) rowView.getTag();
			}
			
			holder.labelView.setText(labels[position] + ":");
			holder.valueView.setText(values[position]);

			return rowView;
		}
	}
	
	private static String getFriendlyTimeDifference(Date startTime, Date endTime) {
        StringBuffer sb = new StringBuffer();
        long diffInSeconds = (endTime.getTime() - startTime.getTime()) / 1000;

        long sec = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
        long min = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
        long hrs = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;
        long days = (diffInSeconds = (diffInSeconds / 24)) >= 30 ? diffInSeconds % 30 : diffInSeconds;
        long months = (diffInSeconds = (diffInSeconds / 30)) >= 12 ? diffInSeconds % 12 : diffInSeconds;
        long years = (diffInSeconds = (diffInSeconds / 12));

        if (years > 0) {
            if (years == 1) {
                sb.append("a year");
            } else {
                sb.append(years + " years");
            }
            if (years <= 6 && months > 0) {
                if (months == 1) {
                    sb.append(" and a month");
                } else {
                    sb.append(" and " + months + " months");
                }
            }
        } else if (months > 0) {
            if (months == 1) {
                sb.append("a month");
            } else {
                sb.append(months + " months");
            }
            if (months <= 6 && days > 0) {
                if (days == 1) {
                    sb.append(" and a day");
                } else {
                    sb.append(" and " + days + " days");
                }
            }
        } else if (days > 0) {
            if (days == 1) {
                sb.append("a day");
            } else {
                sb.append(days + " days");
            }
            if (days <= 3 && hrs > 0) {
                if (hrs == 1) {
                    sb.append(" and an hour");
                } else {
                    sb.append(" and " + hrs + " hours");
                }
            }
        } else if (hrs > 0) {
            if (hrs == 1) {
                sb.append("an hour");
            } else {
                sb.append(hrs + " hours");
            }
            if (min > 1) {
                sb.append(" and " + min + " minutes");
            }
        } else if (min > 0) {
            if (min == 1) {
                sb.append("a minute");
            } else {
                sb.append(min + " minutes");
            }
            if (sec > 1) {
                sb.append(" and " + sec + " seconds");
            }
        } else {
            if (sec <= 1) {
                sb.append("about a second");
            } else {
                sb.append("about " + sec + " seconds");
            }
        }

        return sb.toString();
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		getMenuInflater().inflate(R.menu.shared_options, menu);

		MenuItem menuItem = menu.findItem(R.id.preferences_menu);
		menuItem.setIntent(new Intent(this, MyPreferences.class));

		menuItem = menu.findItem(R.id.about_menu);
		menuItem.setIntent(new Intent(this, AboutActivity.class));

		return true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		Units.instance.setFromPrefs(this);
		displayData();
	}
}
