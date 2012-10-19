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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import android.content.Intent;
import android.os.Bundle;

public class FlightSummary {
	private static String EXTRA_STARTTIME = "com.geeksville.gaggle.STARTTIME";
	private static String EXTRA_ENDTIME = "com.geeksville.gaggle.ENDTIME";
	private static String EXTRA_STARTALTITUDE = "com.geeksville.gaggle.STARTALTITUDE";
	private static String EXTRA_MAXALTITUDEAFTERLAUNCH = "com.geeksville.gaggle.MAXALTITUDEAFTERLAUNCH";
	private static String EXTRA_MAXGROUNDSPEED = "com.geeksville.gaggle.MAXGROUNDSPEED";
	private static String EXTRA_AVERAGEGROUNDSPEED = "com.geeksville.gaggle.AVERAGEGROUNDSPEED";
	private static String EXTRA_TOTALGROUNDDISTANCE = "com.geeksville.gaggle.TOTALGROUNDDISTANCE";
	private static String EXTRA_TOTALVERTICALDISTANCE = "com.geeksville.gaggle.TOTALVERTICALDISTANCE";
	private static String EXTRA_MAXDISTANCEFROMLAUNCH = "com.geeksville.gaggle.MAXDISTANCEFROMLAUNCH";
	
	private long startTime;
	private long endTime;
	private float startAltitude;
	private float maxAltitudeAfterLaunch;
	private float maxGroundSpeed;
	private float averageGroundSpeed;
	private double totalGroundDistance;
	private float totalVerticalDistance;
	private double maxDistanceFromLaunch;
	
	private Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
	
	public FlightSummary() {
	}
	
	public FlightSummary(Bundle data) {
		setStartTime(data.getLong(EXTRA_STARTTIME));
		setEndTime(data.getLong(EXTRA_ENDTIME));
		setStartAltitude(data.getFloat(EXTRA_STARTALTITUDE));
		setMaxAltitudeAfterLaunch(data.getFloat(EXTRA_MAXALTITUDEAFTERLAUNCH));
		setMaxGroundSpeed(data.getFloat(EXTRA_MAXGROUNDSPEED));
		setAverageGroundSpeed(data.getFloat(EXTRA_AVERAGEGROUNDSPEED));
		setTotalGroundDistance(data.getDouble(EXTRA_TOTALGROUNDDISTANCE));
		setTotalVerticalDistance(data.getFloat(EXTRA_TOTALVERTICALDISTANCE));
		setMaxDistanceFromLaunch(data.getDouble(EXTRA_MAXDISTANCEFROMLAUNCH));
	}
	
	public void addDataToIntent(Intent i) {
		i.putExtra(EXTRA_STARTTIME, getStartTime());
		i.putExtra(EXTRA_ENDTIME, getEndTime());
		i.putExtra(EXTRA_STARTALTITUDE, getStartAltitude());
		i.putExtra(EXTRA_MAXALTITUDEAFTERLAUNCH, getMaxAltitudeAfterLaunch());
		i.putExtra(EXTRA_MAXGROUNDSPEED, getMaxGroundSpeed());
		i.putExtra(EXTRA_AVERAGEGROUNDSPEED, getAverageGroundSpeed());
		i.putExtra(EXTRA_TOTALGROUNDDISTANCE, getTotalGroundDistance());
		i.putExtra(EXTRA_TOTALVERTICALDISTANCE, getTotalVerticalDistance());
		i.putExtra(EXTRA_MAXDISTANCEFROMLAUNCH, getMaxDistanceFromLaunch());
	}
	
	public void setStartTime(long startTime) { this.startTime = startTime; }
	public long getStartTime() { return this.startTime; }
	public void setEndTime(long endTime) { this.endTime = endTime; }
	public long getEndTime() { return this.endTime; }
	public float getStartAltitude() { return this.startAltitude; }
	public void setStartAltitude(float startAltitude) { this.startAltitude = startAltitude; }
	public float getMaxAltitudeAfterLaunch() { return this.maxAltitudeAfterLaunch; }
	public void setMaxAltitudeAfterLaunch(float altitude) { this.maxAltitudeAfterLaunch = altitude; }
	public float getMaxGroundSpeed() { return this.maxGroundSpeed; }
	public void setMaxGroundSpeed(float maxGroundSpeed) { this.maxGroundSpeed = maxGroundSpeed; }
	public float getAverageGroundSpeed() { return this.averageGroundSpeed; }
	public void setAverageGroundSpeed(float averageGroundSpeed) { this.averageGroundSpeed = averageGroundSpeed; }
	public double getTotalGroundDistance() { return this.totalGroundDistance; }
	public void setTotalGroundDistance(double totalGroundDistance) { this.totalGroundDistance = totalGroundDistance; }
	public float getTotalVerticalDistance() { return this.totalVerticalDistance; }
	public void setTotalVerticalDistance(float totalVerticalDistance) { this.totalVerticalDistance = totalVerticalDistance; }
	public double getMaxDistanceFromLaunch() { return this.maxDistanceFromLaunch; }
	public void setMaxDistanceFromLaunch(double maxDistanceFromLaunch) { this.maxDistanceFromLaunch = maxDistanceFromLaunch; }
	
	public Date getStartDate() { return getDate(this.startTime); }
	public Date getEndDate() { return getDate(this.endTime); }
	
	private Date getDate(long millis) {
		cal.setTimeInMillis(millis);
		return cal.getTime();
	}
}
