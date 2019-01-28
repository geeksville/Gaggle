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

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Basic DB access for our DB of stored flights and points
 * 
 * @author kevinh The DB is structured with the following tables:
 * 
 *         flightinfo has _id, title, pilotname, starttime, endtime, notes
 *         (str), uploaded (bool), first_point_id, last_point_id
 * 
 *         locinfo has _id, time, lat, long, alt, heading, gndtrack, gndspeed,
 *         airspd
 * 
 *         route
 * 
 *         waypoint
 * 
 *         rcontent
 * 
 *         FIXME - I probably need to add indexes for the rcontent join columns
 */
public class LocationLogDbAdapter {

  /**
   * Common key to all tables
   */
  public static final String KEY_ROWID = "_id";

  /**
   * Keys for fltinfo
   */
  public static final String KEY_NAME = "name",
      KEY_FLT_PILOTNAME = "pilotname", KEY_FLT_STARTTIME = "starttime",
      KEY_FLT_ENDTIME = "endtime", KEY_DESCRIPTION = "description",
      KEY_FLT_WANTUP = "wantupload", KEY_FLT_UPLOADED = "uploaded";

  private static final String FLTINFO_CREATE = "create table fltinfo ("
      + "_id integer primary key autoincrement, " + "name text, " // Defaults
      // to
      // the
      // start
      // time
      + "pilotname text, " + "starttime integer, " // msecs since 1970
      + "endtime integer, " // may be null if flight not over
      + "description text, " + "wantupload boolean, " // true if we'd like
      // this
      // sent when possible
      + "uploaded boolean);";

  private static final String FLTINFO_TABLE = "fltinfo";

  /**
   * Keys for locinfo
   */
  public static final String KEY_LOC_FLTID = "fltid", KEY_LOC_TIME = "time",
      KEY_LATITUDE = "latitude", KEY_LONGITUDE = "longitude",
      KEY_ALTITUDE = "altitude", KEY_LOC_HEADING = "heading",
      KEY_LOC_GNDSPEED = "gndspeed", KEY_LOC_GNDTRACK = "gndtrack",
      KEY_LOC_AIRSPEED = "airspeed", KEY_LOC_ACCX = "accx",
      KEY_LOC_ACCY = "accy", KEY_LOC_ACCZ = "accz", KEY_LOC_VSPD = "vspd";

  /**
   * It might have made more sense to have a schema where the flight has start
   * and end locinfo ids, but I was lazy and I guess that penalized me with
   * wasting 16 bytes per location. FIXME this also prevents showing # pts on
   * the flt list
   */
  private static final String LOCINFO_CREATE = "create table locinfo ("
      + "_id integer primary key autoincrement, "
      + "fltid integer, " // The flight id for this point
      + "time datetime, " + "latitude double, " + "longitude double, "
      + "altitude integer, " + "heading integer, " + "gndtrack integer, "
      + "gndspeed integer, " + "airspeed integer, " + "accx double, "
      + "accy double, " + "accz double, " + "vspd double, "
      + "FOREIGN KEY(fltid) REFERENCES fltinfo(_id)" + ");";

  private static final String LOCINFO_TABLE = "locinfo";

  /**
   * schema for waypoints
   */
  private static final String WAYPOINT_CREATE = "create table waypoint ("
      + "_id integer primary key autoincrement, " + "latitude double, "
      + "longitude double, " + "altitude integer, "
      + "name text UNIQUE ON CONFLICT REPLACE, " + "type integer, " // the
      // type
      // of
      // waypoint
      // (launch,
      // lz,
      // other
      // etc...
      // - null for unspecified)
      + "description text);";

  private static final String WAYPOINT_TABLE = "waypoint";

  public static final String KEY_WAYPOINT_TYPE = "type";

  /**
   * schema for routes
   * 
   * One table for the information on the routes A second table with keys used
   * to find each waypoint along the route (for joins)
   */
  private static final String ROUTE_CREATE = "create table route ("
      + "_id integer primary key autoincrement, " + "name text);";

  private static final String ROUTE_TABLE = "route";

  private static final String RCONTENTS_CREATE = "create table rcontent ("
      + "_id integer primary key autoincrement, "
      + "routeid integer, "
      + "waypointid integer, "
      + "diameter integer, " // The diameter of the waypoint in meters
      // (for comp cylinder purposes)
      + "FOREIGN KEY(routeid) REFERENCES route(_id), "
      + "FOREIGN KEY(waypointid) REFERENCES waypoint(_id)" + ");";

  private static final String RCONTENTS_TABLE = "rcontent";

  private static final String DATABASE_NAME = "data";

  /**
   * Debugging tag
   */
  private static final String TAG = "LocationLogDbAdapter";

  private Context context;
  private DatabaseHelper dbHelper;
  private SQLiteDatabase db;

  /**
   * Constructor - takes the context to allow the database to be opened/created
   * 
   * @param ctx
   *          the Context within which to work
   */
  public LocationLogDbAdapter(Context ctx) {
    context = ctx;

    open();
  }

  /**
   * Open our DB
   * 
   * @throws SQLException
   */
  private void open() throws SQLException {
    dbHelper = new DatabaseHelper(context);

    db = dbHelper.getWritableDatabase();
  }

  /**
   * FIXME - figure out if java has IDisposable or IClosable or somesuch...
   */
  public void close() {
    db.close();
  }

  /**
   * Create a new flight record, with the minimum amount of init data
   * 
   * @return
   */
  public long createFlight(String pilotname, String notes, long startTime) {
    ContentValues vals = new ContentValues();

    // We are no longer using this field in the DB, instead, we generate it
    // when browsing in the GUI (more flexible)

    // vals.put(KEY_NAME, title);
    vals.put(KEY_FLT_PILOTNAME, pilotname);
    if (notes != null)
      vals.put(KEY_DESCRIPTION, notes);
    vals.put(KEY_FLT_STARTTIME, startTime);
    vals.put(KEY_FLT_UPLOADED, 0);

    return db.insert(FLTINFO_TABLE, null, vals);
  }

  /**
   * Update a flight record
   * 
   * @param id
   * @param endtime
   *          null for no value
   * @param notes
   *          null for no value
   * @param uploaded
   *          null for no update
   * @param lastpoint_id
   *          null for no value
   */
  public void updateFlight(long id, Date endtime, String notes, Boolean wantup,
      Boolean uploaded) {
    ContentValues vals = new ContentValues();

    if (endtime != null)
      vals.put(KEY_FLT_ENDTIME, endtime.getTime());
    if (notes != null)
      vals.put(KEY_DESCRIPTION, notes);
    if (wantup != null)
      vals.put(KEY_FLT_WANTUP, wantup.booleanValue() ? 1 : 0);
    if (uploaded != null)
      vals.put(KEY_FLT_UPLOADED, uploaded.booleanValue() ? 1 : 0);

    String whereClause = KEY_ROWID + "=" + id;
    String[] whereArgs = null;
    db.update(FLTINFO_TABLE, vals, whereClause, whereArgs);
  }

  public long addLocation(long fltid, long time, double latitude,
      double longitude, float altitude, int heading, float groundspeed,
      float[] accel, float vspd) {
    ContentValues vals = new ContentValues();

    vals.put(KEY_LOC_FLTID, fltid);
    vals.put(KEY_LOC_TIME, time);
    vals.put(KEY_LATITUDE, latitude);
    vals.put(KEY_LONGITUDE, longitude);
    vals.put(KEY_ALTITUDE, Float.isNaN(altitude) ? null : (int) altitude);
    vals.put(KEY_LOC_HEADING, heading);
    vals.put(KEY_LOC_GNDSPEED, Float.isNaN(groundspeed) ? null
        : (int) groundspeed);

    if (!Float.isNaN(vspd))
      vals.put(KEY_LOC_VSPD, vspd);

    if (accel != null) {
      vals.put(KEY_LOC_ACCX, accel[0]);
      vals.put(KEY_LOC_ACCY, accel[1]);
      vals.put(KEY_LOC_ACCZ, accel[2]);
    }

    return db.insert(LOCINFO_TABLE, null, vals);
  }

  /**
   * Delete the flight with a given id
   * 
   * @param fltid
   */
  public void deleteFlight(long fltid) {
    String whereClause = KEY_ROWID + "=" + fltid; // FIXME, use the
    // whereArgs to avoid
    // SQL injection
    // problems
    String[] whereArgs = null;

    db.beginTransaction();
    db.delete(FLTINFO_TABLE, whereClause, whereArgs);

    whereClause = KEY_LOC_FLTID + "=" + fltid;
    db.delete(LOCINFO_TABLE, whereClause, whereArgs);

    db.setTransactionSuccessful();
    db.endTransaction();
  }

  /**
   * 
   * @return A Cursor that must be closed by the caller.
   */
  public Cursor fetchAllFlights() {
    return fetchFlight(null);
  }

  /**
   * Return a given flight id
   * 
   * @return A Cursor that must be closed by the caller.
   */
  public Cursor fetchFlight(long fltid) {
    String whereClause = KEY_ROWID + "=" + fltid; // FIXME, use the

    return fetchFlight(whereClause);
  }

  /**
   * Return an arbitrary set of flights
   * 
   * @return A Cursor that must be closed by the caller.
   */
  private Cursor fetchFlight(String whereClause) {
    Cursor cursor = db.query(FLTINFO_TABLE, new String[] { KEY_ROWID,
        KEY_FLT_PILOTNAME, KEY_NAME, KEY_FLT_STARTTIME, KEY_FLT_ENDTIME,
        KEY_DESCRIPTION, KEY_FLT_WANTUP, KEY_FLT_UPLOADED }, whereClause, null,
        null, null, null);
    if (cursor != null)
      cursor.moveToFirst();

    return cursor;
  }

  /**
   * Get all the tracklog points for a given flight (ordered by time)
   * 
   * @param fltid
   * @return A Cursor that must be closed by the caller.
   */
  public Cursor fetchLocations(long fltid) {
    String whereClause = KEY_LOC_FLTID + "=" + fltid;
    String[] whereArgs = null;
    String orderBy = KEY_LOC_TIME;

    Cursor cursor = db.query(LOCINFO_TABLE, new String[] { KEY_LOC_TIME,
        KEY_LATITUDE, KEY_LONGITUDE, KEY_ALTITUDE, KEY_LOC_HEADING,
        KEY_LOC_GNDSPEED, KEY_LOC_GNDTRACK, KEY_LOC_AIRSPEED, KEY_LOC_ACCX,
        KEY_LOC_ACCY, KEY_LOC_ACCZ, KEY_LOC_VSPD }, whereClause, whereArgs,
        null, null, orderBy);
    if (cursor != null)
      cursor.moveToFirst();

    return cursor;
  }

  /**
   * Get all the waypoints (ordered by name)
   * 
   * @param fltid
   * @return A Cursor that must be closed by the caller.
   * 
   *         FIXME, make a version that restricts the search to lie between a
   *         specified lat/long range
   */
  Cursor fetchWaypoints() {
    String whereClause = null;
    String[] whereArgs = null;
    String orderBy = KEY_NAME;

    Cursor cursor = db.query(WAYPOINT_TABLE, new String[] { KEY_ROWID,
        KEY_LATITUDE, KEY_LONGITUDE, KEY_ALTITUDE, KEY_NAME, KEY_DESCRIPTION,
        KEY_WAYPOINT_TYPE }, whereClause, whereArgs, null, null, orderBy);
    if (cursor != null)
      cursor.moveToFirst();

    return cursor;
  }

  /**
   * Add a new waypoint
   * 
   * @param name
   * @param desc
   * @param latitude
   * @param longitude
   * @param altitude
   *          Or NaN for unknown
   * @return Return our new rowid
   */
  long addWaypoint(String name, String desc, double latitude, double longitude,
      float altitude, int type) {
    ContentValues vals = new ContentValues();

    vals.put(KEY_NAME, name);
    vals.put(KEY_DESCRIPTION, desc);
    vals.put(KEY_LATITUDE, latitude);
    vals.put(KEY_LONGITUDE, longitude);
    vals.put(KEY_ALTITUDE, Float.isNaN(altitude) ? null : (int) altitude);
    vals.put(KEY_WAYPOINT_TYPE, type);

    return db.insert(WAYPOINT_TABLE, null, vals);
  }

  /**
   * Delete the flight with a given id
   * 
   * @param fltid
   */
  void deleteWaypoint(long id) {
    String whereClause = KEY_ROWID + "=" + id;
    deleteWaypoint(whereClause);
  }

  /**
   * Delete the flight with a given id
   * 
   * @param fltid
   */
  public void deleteAllWaypoints() {
    deleteWaypoint(null);
  }

  private void deleteWaypoint(String whereClause) {
    String[] whereArgs = null;

    db.beginTransaction();
    int numdeleted = db.delete(WAYPOINT_TABLE, whereClause, whereArgs);

    db.setTransactionSuccessful();
    db.endTransaction();
  }

  public void updateWaypoint(long id, String name, String description,
      double latitude, double longitude, float altitude, int waypointType) {
    ContentValues vals = new ContentValues();

    vals.put(KEY_NAME, name);
    vals.put(KEY_DESCRIPTION, description);
    vals.put(KEY_LATITUDE, latitude);
    vals.put(KEY_LONGITUDE, longitude);
    vals.put(KEY_ALTITUDE, Float.isNaN(altitude) ? null : (int) altitude);
    vals.put(KEY_WAYPOINT_TYPE, waypointType);

    String whereClause = KEY_ROWID + "=" + id;
    String[] whereArgs = null;
    db.update(WAYPOINT_TABLE, vals, whereClause, whereArgs);

  }

  private static class DatabaseHelper extends SQLiteOpenHelper {

    /**
     * Added three acceleration columns
     */
    private static final int addedAccelVersion = 6;

    /**
     * Added three acceleration columns
     */
    private static final int addedVSpdVersion = 7;

    /**
     * The android version # that we can accept without needing to upgrade DB
     */
    private static final int VERSION = 7;

    DatabaseHelper(Context context) {
      super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

      db.execSQL(FLTINFO_CREATE);
      db.execSQL(LOCINFO_CREATE);
      db.execSQL(WAYPOINT_CREATE);
      db.execSQL(ROUTE_CREATE);
      db.execSQL(RCONTENTS_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      if (oldVersion >= VERSION)
        Log.d(TAG, "Skipping DB upgrade, schema has not changed.");
      else {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
            + newVersion);

        if (oldVersion < addedAccelVersion) {
          db.execSQL("ALTER TABLE " + LOCINFO_TABLE + " ADD accx double");
          db.execSQL("ALTER TABLE " + LOCINFO_TABLE + " ADD accy double");
          db.execSQL("ALTER TABLE " + LOCINFO_TABLE + " ADD accz double");
        }

        if (oldVersion < addedVSpdVersion) {
          db.execSQL("ALTER TABLE " + LOCINFO_TABLE + " ADD vspd double");
        }

        // Log.w(TAG, "Upgrading database from version " + oldVersion +
        // " to " + newVersion
        // + ", which will destroy all old data");

        // db.execSQL("DROP TABLE IF EXISTS " + LOCINFO_TABLE);
        // db.execSQL("DROP TABLE IF EXISTS " + FLTINFO_TABLE);
        // db.execSQL("DROP TABLE IF EXISTS " + WAYPOINT_TABLE);
        // db.execSQL("DROP TABLE IF EXISTS " + ROUTE_TABLE);
        // db.execSQL("DROP TABLE IF EXISTS " + RCONTENTS_TABLE);
        //
        // onCreate(db);
      }
    }
  }
}
