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
package com.geeksville.location;

import com.geeksville.info.FlightSummary;

public class SummaryWriter implements PositionWriter {
  private boolean firstPosition = true;

  private long endTime;
  private float maxAltitudeAfterLaunch;
  private float maxGroundSpeed;
  private float totalGroundSpeed;
  private double totalGroundDistance;
  private float totalVerticalDistance;
  private double maxDistanceFromLaunch;
  private int positionCount;

  private double firstLatitude;
  private double firstLongitude;
  private double lastLatitude;
  private double lastLongitude;
  private float lastAltitude;

  private FlightSummary summary;

  // accel is [0] - xaccel, [1] - yaccel, [2] - zaccel
  // test on device

  public SummaryWriter(FlightSummary summary) {
    this.summary = summary;
  }

  @Override
  public void emitProlog() {
  }

  @Override
  public void emitPosition(long time, double latitude, double longitude,
      float altitude, int bearing, float groundSpeed, float[] accel, float vspd) {
    if (firstPosition) {
      summary.setStartTime(time);
      summary.setStartAltitude(altitude);
      firstLatitude = latitude;
      firstLongitude = longitude;
      lastLatitude = latitude;
      lastLongitude = longitude;
      lastAltitude = altitude;

      firstPosition = false;
    } else {
      endTime = time;

      if (altitude > maxAltitudeAfterLaunch)
        maxAltitudeAfterLaunch = altitude;

      totalGroundDistance += LocationUtils.LatLongToMeter(lastLatitude,
          lastLongitude, latitude, longitude);
      lastLatitude = latitude;
      lastLongitude = longitude;

      double currentDistanceFromLaunch = LocationUtils.LatLongToMeter(
          firstLatitude, firstLongitude, latitude, longitude);
      if (currentDistanceFromLaunch > maxDistanceFromLaunch)
        maxDistanceFromLaunch = currentDistanceFromLaunch;

      totalVerticalDistance += Math.abs(lastAltitude - altitude);
      lastAltitude = altitude;
    }

    totalGroundSpeed += groundSpeed;

    if (groundSpeed > maxGroundSpeed)
      maxGroundSpeed = groundSpeed;

    positionCount++;
  }

  @Override
  public void emitEpilog() {
    summary.setEndTime(endTime);
    summary.setMaxAltitudeAfterLaunch(maxAltitudeAfterLaunch);
    summary.setMaxGroundSpeed(maxGroundSpeed);
    summary.setAverageGroundSpeed(totalGroundSpeed / (float) positionCount);
    summary.setTotalGroundDistance(totalGroundDistance);
    summary.setTotalVerticalDistance(totalVerticalDistance);
    summary.setMaxDistanceFromLaunch(maxDistanceFromLaunch);
  }
}
