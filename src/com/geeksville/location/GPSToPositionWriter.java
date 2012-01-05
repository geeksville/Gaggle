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
package com.geeksville.location;

import java.util.Observable;
import java.util.Observer;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import com.geeksville.android.ChangeHandler;
import com.geeksville.gaggle.GagglePrefs;
import com.geeksville.gaggle.R;

/**
 * Reads positions from the GPS and writes them to a PositionWriter (probably a
 * log file)
 * 
 * @author kevinh
 * 
 */
public class GPSToPositionWriter extends AbstractLocationListener implements
    ServiceConnection, Observer {

  private Location initialPos;

  private PositionWriter dest;

  private Context context;

  private IGPSClient gps;

  private int pollInterval;

  private static final String TAG = "GPSToPositionWriter";

  private int numPoints;

  /**
   * Distance in meters from initial position before we'll detect it as a launch
   */
  private int launchDistanceY, launchDistanceX;

  /**
   * How many meters to a degree (approx)
   */
  private static final double metersLatitudePerDeg = 111131.745;
  private static final double metersLongitudePerDeg = 78846.80572069259;

  /**
   * The person watching us
   */
  ChangeHandler myObserver;

  AccelerometerClient accel = null;
  BarometerClient baro = null;

  public void setObserver(ChangeHandler obs) {
    myObserver = obs;
  }

  private void setStatus(Status stat) {
    curStatus = stat;
    if (myObserver != null)
      myObserver.onChanged(this);
  }

  public enum Status {
    OFF, WAIT_FOR_LOCK, WAIT_FOR_LAUNCH, IN_FLIGHT, LANDED
  }

  private Status curStatus = Status.OFF;

  public GPSToPositionWriter(Context _context) {
    context = _context;
  }

  public Status getStatus() {
    return curStatus;
  }

  public boolean isLogging() {
    return curStatus != Status.OFF && curStatus != Status.LANDED;
  }

  /**
   * A human readable description of our status
   * 
   * @return
   */
  public String getStatusString() {
    switch (curStatus) {
    case OFF:
      return context.getString(R.string.off);
    case WAIT_FOR_LOCK:
      return context.getString(R.string.acquiring_gps_fix);
    case WAIT_FOR_LAUNCH:
      return String.format(
          context.getString(R.string.waiting_for_launch_d_pts), numPoints);
    case IN_FLIGHT:
      return String.format(context.getString(R.string.in_flight_d_pts),
          numPoints);
    case LANDED:
      return context.getString(R.string.landed);
    }

    throw new IllegalStateException("Unknown GPS logging state");
  }

  /**
   * Stop logging
   */
  public synchronized void stopLogging() {
    if (isLogging()) {

      gps.removeLocationListener(this);

      if (curStatus == Status.IN_FLIGHT) {
        dest.emitEpilog();
        // flightStopTime = new Date();
      }

      gps.stopForeground();

      // If we never started flying, just return to off
      setStatus(curStatus == Status.IN_FLIGHT ? Status.LANDED : Status.OFF);

      GPSClient.unbindFrom(context, this);

      if (accel != null) {
        accel.deleteObserver(this);
        accel = null;
      }

      if (baro != null) {
        baro.deleteObserver(this);
        baro = null;
      }
    }
  }

  public void startLogging(Context context, PositionWriter dest,
      int pollIntervalSecs, int launchDistanceX, int launchDistanceY) {
    this.context = context;

    if (!isLogging()) {
      accel = AccelerometerClient.create(context);
      if (accel != null)
        accel.addObserver(this);

      try {
        baro = BarometerClient.create(context);
      } catch (VerifyError ex) {
        Log.e(TAG, "Not on 1.5: " + ex);
      }
      if (baro != null)
        baro.addObserver(this);

      this.numPoints = 0;
      this.pollInterval = pollIntervalSecs;
      this.dest = dest;

      this.launchDistanceX = launchDistanceX;
      this.launchDistanceY = launchDistanceY;

      GPSClient.bindTo(context, this);
    } else
      throw new IllegalStateException("Already logging");
  }

  @Override
  public void onServiceConnected(ComponentName name, IBinder service) {
    gps = (IGPSClient) service;

    // FIXME - move to correct place
    gps.startForeground(context.getString(R.string.tracklog_started),
        context.getString(R.string.capturing_tracklog));

    setStatus(Status.WAIT_FOR_LOCK);

    GagglePrefs prefs = new GagglePrefs(context);

    long minTime = prefs.getGPSUpdateFreq() * 1000; // num msec between events
    float minDist = prefs.getGPSUpdateDist(); // need at least this many meters

    gps.addLocationListener(minTime, minDist, this);
  }

  @Override
  public void onServiceDisconnected(ComponentName name) {
    // TODO Auto-generated method stub

  }

  /**
   * Called after we have a GPS lock
   */
  private void newStateWaitForLaunch() {
    setStatus(Status.WAIT_FOR_LAUNCH);
  }

  /**
   * Called once we've determined we've moved far enough from launch
   * 
   * @param initialPoints
   *          points we've saved up that should now be added to the tracklog
   */
  private synchronized void newStateFlight(Location[] initialPoints) {

    // flightStartTime = new Date();

    setStatus(Status.IN_FLIGHT);

    dest.emitProlog();
    for (Location l : initialPoints)
      emitPosition(l);
  }

  /**
   * Add a point to the tracklog
   * 
   * @param location
   */
  private void emitPosition(Location location) {
    double lat = location.getLatitude();
    double longitude = location.getLongitude();
    float kmPerHr = location.hasSpeed() ? (float) (location.getSpeed() * 3.6)
        : Float.NaN;
    // convert m/sec to km/hr

    float[] accelVals = (accel != null) ? accel.getValues() : null;
    float vspd = (baro != null) ? baro.getVerticalSpeed() : Float.NaN;

    // The emulator will falsely claim 0 for the first point reported -
    // skip it
    if (lat != 0.0)
      dest.emitPosition(location.getTime(), lat, longitude,
          location.hasAltitude() ? (float) location.getAltitude() : Float.NaN,
          (int) location.getBearing(), kmPerHr, accelVals, vspd);
  }

  @Override
  public void onLocationChanged(Location location) {

    numPoints++;
    setStatus(curStatus); // For debugging

    switch (curStatus) {
    case OFF: // Ignore - stale message
      break;

    case WAIT_FOR_LOCK:
      // We need a 3d position before we can even start moving
      if (location.hasAltitude()) {
        initialPos = location;
        newStateWaitForLaunch(); // We must now have a lock
      }
      break;

    case WAIT_FOR_LAUNCH:
      if (location.hasAltitude()) {
        int deltay = Math.abs(((int) initialPos.getAltitude())
            - ((int) location.getAltitude()));

        double metersLat = metersLatitudePerDeg
            * Math.abs(initialPos.getLatitude() - location.getLatitude());
        double metersLong = metersLongitudePerDeg
            * Math.abs(initialPos.getLongitude() - location.getLongitude());

        /*
         * Too low a precision to be useful (at my house it shows 2000 meter
         * distance int deltax = (int) LocationUtils.LatLongToMeter((float)
         * initialPos.getLatitude(), (float) initialPos .getLongitude(), (float)
         * location.getLatitude(), (float) location .getLongitude());
         */

        if (launchDistanceY == 0 || launchDistanceX == 0
            || metersLat >= launchDistanceX || metersLong >= launchDistanceX
            || deltay >= launchDistanceY) {
          Log.i(TAG, String.format("Launch detected dx=%f, dy=%f, dz=%d",
              metersLong, metersLat, deltay));
          newStateFlight(new Location[] { initialPos, location });
        }
      }
      break;

    case IN_FLIGHT:
      emitPosition(location);
      break;

    case LANDED: // Ignore - stale message
      break;
    }
  }

  /**
   * If onProviderDisabled called, then stop track log
   */
  @Override
  public void onProviderDisabled(String provider) {
    stopLogging();
  }

  @Override
  public void update(Observable observable, Object data) {
    // TODO Auto-generated method stub

  }

}
