/**
 * 
 */
package com.geeksville.location;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.GpsStatus.NmeaListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.geeksville.gaggle.AudioVario;
import com.geeksville.gaggle.GagglePrefs;
import com.geeksville.gaggle.R;
import com.geeksville.gaggle.TopActivity;

/**
 * Log locations to a file, db or other.
 * 
 * @author kevinh
 * 
 *         Effectively the 'glue' between generic Java PositionWriter and the
 *         android APIs
 */
public class GPSClient extends Service implements IGPSClient {

  /**
   * Debugging tag
   */
  private static final String TAG = "GPSClient";

  
  public SharedPreferences mSharedPreferences;
  // Listener defined by anonymous inner class.
  public OnSharedPreferenceChangeListener mListener = new OnSharedPreferenceChangeListener() {        

      @Override
      public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
  		if(key.equals("altitude_correction")){
  			final String val = sharedPreferences.getString("altitude_correction", "none");
  			
  			if (val.equals("nmea")){
  				Log.d(TAG, "Enabling NMEA");
  	  			geoid_correction = null;
  	  			manager.addNmeaListener(nmea_listener);
  			} else if (val.equals("egm84")){
  	  			try {
  	  				Log.d(TAG, "Enabling EGM84");
  	  				initGeoid();
  	  			} catch (IOException e){
  	  				Log.d(TAG, "Can't init egm84 geoid correction", e);
  	  			}
  	  			manager.removeNmeaListener(nmea_listener);
  			} else {
  				Log.d(TAG, "Disabling all geoid correction");
  	  			geoid_correction = null;
  	  			manager.removeNmeaListener(nmea_listener);
  			}
  		}
      }
  };

  private MyBinder binder = new MyBinder();

  private HandlerThread thread = new HandlerThread("GPSClient");

  private LocationManager manager;

  private class UpdateFreq {
    public int minDist;
    public long minTime;

    long lastUpdateTime = 0;

    /**
     * 
     * @param time
     * @param dist
     */
    public UpdateFreq(long time, int dist) {
      minDist = dist;
      minTime = time;
    }
  }

  public static GPSClient instance;

  private HashMap<LocationListener, UpdateFreq> listeners = new HashMap<LocationListener, UpdateFreq>();

  private long minTimePerUpdate = Long.MAX_VALUE;

  private float minDistPerUpdate = Float.MAX_VALUE;

  /**
   * In developer mode, this provides our data
   */
  private TestGPSDriver simData = null;

  PrintStream alti_debug_file = null;
  
  private int currentStatus = OUT_OF_SERVICE;

  // / Once we get a GPS altitude we will fixup the barometer
  private boolean hasSetBarometer = false;
  private IBarometerClient baro = null;
  private AudioVario vario;

  /**
   * Older SDKs don't define LocationProvider.AVAILABLE etc...
   */
  private static final int AVAILABLE = 2, OUT_OF_SERVICE = 0,
      TEMPORARILY_UNAVAILABLE = 1;

  private static Map<Context, Republisher> clients = new HashMap<Context, Republisher>();

  private boolean galaxys_leap_year_fix;

  public GPSClient() {
    try {
      vario = new AudioVario();
    } catch (VerifyError ex) {
      Log.e(TAG, "Not supported on 1.5: " + ex);
    }
  }

  /**
   * Glue to bind to this service
   * 
   * @param context
   *          the client's context
   * @param client
   *          who to notify
   */
  public static void bindTo(Context context, ServiceConnection client) {
    // FIXME - only ONE ServiceConnection client, the most recent one gets
    // to find out about changes. keep a list of my own? Or perhaps I should
    // rely more on the automatic relationship
    // between the Context's lifecycle and the service life cycle. For right
    // now I'll use the list of my own

    // FIXME - always use the application context, because that's what
    // really gets tracked anyway (from looking at
    // android source)
    context = context.getApplicationContext();

    Republisher conns = clients.get(context);
    if (conns == null) {
      conns = new Republisher();
      conns.add(client);
      clients.put(context, conns);

      // We've never heard from this context before
      context.bindService(new Intent(context, GPSClient.class), conns,
          Service.BIND_AUTO_CREATE | Service.BIND_DEBUG_UNBIND);
    } else
      conns.add(client);
  }

  /**
   * Unregister this client
   * 
   * @param client
   */
  public static void unbindFrom(Context context, ServiceConnection client) {

    // FIXME - always use the application context, because that's what
    // really gets tracked anyway (from looking at
    // android source)
    context = context.getApplicationContext();

    Republisher conns = clients.get(context);
    if (conns == null) {
      Log.e(TAG, "missing context GPSClient.unbindFrom"); // FIXME -
      // figure out
      // why this
      // occurs
      // sometimes
      return;
    }

    // Done with this context?
    if (!conns.remove(client)) {
      clients.remove(context);
      context.unbindService(conns);
    }
  }

  private static class Republisher implements ServiceConnection {

    Set<ServiceConnection> clients = new HashSet<ServiceConnection>();

    ComponentName name;
    IBinder service;

    public synchronized void add(ServiceConnection conn) {
      if (clients.contains(conn)) {
        Log.e(TAG, "redundant GPSClient.bindTo"); // FIXME - why does
        // this happen?
        return;
      }

      clients.add(conn);

      // If we are already connected, tell the new client now
      if (service != null)
        conn.onServiceConnected(name, service);
    }

    public synchronized boolean remove(ServiceConnection conn) {
      if (!clients.remove(conn))
        Log.e(TAG, "missing client GPSClient.unbindFrom");

      return clients.size() != 0;
    }

    @Override
    public synchronized void onServiceConnected(ComponentName name,
        IBinder service) {
      this.name = name;
      this.service = service;

      for (ServiceConnection c : clients)
        c.onServiceConnected(name, service);
    }

    @Override
    public synchronized void onServiceDisconnected(ComponentName name) {
      this.service = null;

      for (ServiceConnection c : clients)
        c.onServiceDisconnected(name);
    }

  }

  private class MyBinder extends Binder implements IGPSClient {

    @Override
    public void addLocationListener(long minTimeMs, float minDistMeters,
        LocationListener l) {
      GPSClient.this.addLocationListener(minTimeMs, minDistMeters, l);
    }

    @Override
    public Location getLastKnownLocation() {
      return GPSClient.this.getLastKnownLocation();
    }

    @Override
    public void removeLocationListener(LocationListener l) {
      GPSClient.this.removeLocationListener(l);

    }

    @Override
    public void startForeground(String tickerMsg, String notificationText) {
      GPSClient.this.startForeground(tickerMsg, notificationText);
    }

    @Override
    public void stopForeground() {
      GPSClient.this.stopForeground();
    }
  }

  private long egm84download_ref = -1;
  private File egm84_local_file= null;
  
  private void initGeoid() throws IOException{
	  // Use download manager to get egm84 data file

	  File sdcard = Environment.getExternalStorageDirectory();
	  String path = getString(R.string.file_folder);

	  egm84_local_file = new File(sdcard, path);
	  if (!egm84_local_file.exists())
		  egm84_local_file.mkdir();
	  // SDCARD/Gaggle
	  
	  egm84_local_file = new File(egm84_local_file, getString(R.string.egm84_altitude_geoid_file));
	  // SDCARD/Gaggle/egm84.ppm
	  if (egm84_local_file.exists()){
		  try {
			  geoid_correction = new GeoIdCorrection(egm84_local_file);
		  } catch (FileNotFoundException e) {
			  Log.d(TAG, "Error when creating GeoIdCorrection",e);
			  geoid_correction = null;
		  }
	  } else {
		  final String url = getString(R.string.egm84_altitude_geoid_url);
		  DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
		  request.setDescription(getString(R.string.egm84_altitude_geoid_file_download_desc));
		  request.setTitle(getString(R.string.egm84_altitude_geoid_file_download_title));

		  IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		  BroadcastReceiver receiver = new BroadcastReceiver(){
			  @Override
			  public void onReceive(Context context, Intent intent) {
				  long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
				  if (reference == egm84download_ref && reference != -1){
					  try {
						  geoid_correction = new GeoIdCorrection(egm84_local_file);
					  } catch (FileNotFoundException e) {
						  Log.d(TAG, "Error when creating GeoIdCorrection",e);
						  geoid_correction = null;
					  }
				  }
			  }
		  };

		  registerReceiver(receiver, filter);
		  request.setDestinationInExternalPublicDir(getString(R.string.file_folder), getString(R.string.egm84_altitude_geoid_file));

		  // get download service and enqueue file
		  DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
		  egm84download_ref = manager.enqueue(request);
	  }
  }
  

  private GeoIdCorrection geoid_correction = null;
  
  /*
   * (non-Javadoc)
   * 
   * @see android.app.Service#onCreate()
   */
  @Override
  public void onCreate() {
    super.onCreate();
    
    final GagglePrefs prefs = GagglePrefs.getInstance();
	if (prefs.isFlurryEnabled())
      FlurryAgent.onStartSession(this, "XBPNNCR4T72PEBX17GKF");

	galaxys_leap_year_fix = prefs.isGalaxySLeapYearBugWorkaroundEnabled();

    instance = this;
    manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    mSharedPreferences.registerOnSharedPreferenceChangeListener(mListener);

    if(prefs.debugAltitudeCorrection()){
  	  File sdcard = Environment.getExternalStorageDirectory();
  	  String path = getString(R.string.file_folder);

  	  File tracklog = new File(sdcard, path);
  	  if (!tracklog.exists())
  		  tracklog.mkdir();
  	  path += '/' + getString(R.string.tracklogs);
  	  tracklog = new File(sdcard, path);
  	  if (!tracklog.exists())
  		  tracklog.mkdir();

  	  File fullname = new File(tracklog, "altidebug.txt");
  	  fullname.delete();

  	  try {
  	  	  fullname.createNewFile();
  		  alti_debug_file = new PrintStream(new FileOutputStream(fullname));
  	  } catch (Exception e) {
  		  Log.d(TAG, "Error when creating altitude debug file",e);
  	  }
    }

    if (prefs.useEGM84Geoid()){
    	try {
    		initGeoid();
    	} catch (IOException e) {
    		Log.d(TAG, "Can't init egm84 geoid correction", e);
    	}
    }

    // Start our looper up
    thread.start();

    try {
      baro = BarometerClient.create(GPSClient.this);

      if (vario != null)
        vario.onCreate(this, thread.getLooper());
    } catch (VerifyError ex) {
      Log.e(TAG, "Not on 1.5: " + ex);
    }

    setSimByPrefs();

    // If the user changes prefs entry switch back to real hw as needed
    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {

      @Override
      public void onSharedPreferenceChanged(
          SharedPreferences sharedPreferences, String key) {
        setSimByPrefs();
      }

    };
    PreferenceManager.getDefaultSharedPreferences(this)
        .registerOnSharedPreferenceChangeListener(listener);
  }

  private void setSimByPrefs() {
    // only use sim in dev mode
    // if (AndroidUtil.isEmulator())

    boolean cursim = simData != null;

    boolean newsim = PreferenceManager.getDefaultSharedPreferences(this)
        .getBoolean("fake_gps_pref", false);

    if (newsim != cursim) {
      if (newsim) {
        try {
          simData = new TestGPSDriver(this);
        } catch (RuntimeException ex) {
          // Tell the user what is going on
          Toast t = Toast.makeText(this, R.string.mock_location_required,
              Toast.LENGTH_LONG);
          t.show();

          Editor e = PreferenceManager.getDefaultSharedPreferences(this).edit();
          e.putBoolean("fake_gps_pref", false);
          e.commit();
        }
      } else {
        simData.close();
        simData = null;
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Service#onDestroy()
   */
  @Override
  public void onDestroy() {
	mSharedPreferences.unregisterOnSharedPreferenceChangeListener(mListener);
    if (vario != null)
      vario.onDestroy();

    // Forcibly unsubscribe anyone still using us
    synchronized (listeners) {
      if (listeners.size() != 0) {
        LocationListener[] tokill = listeners.keySet().toArray(null);
        for (LocationListener l : tokill)
          removeLocationListener(l);

        listeners.clear();
      }
    }
    manager.removeUpdates(listener);

    thread.getLooper().quit();
    
    final GagglePrefs prefs = GagglePrefs.getInstance();
	if (prefs.isFlurryEnabled())
      FlurryAgent.onEndSession(this);

	 if (prefs.debugAltitudeCorrection() && alti_debug_file != null){
   	  alti_debug_file.close();
   	  alti_debug_file = null;
     }

    instance = null;
    super.onDestroy();
  }

  @Override
  public IBinder onBind(Intent intent) {
    return binder;
  }

  private static final int NOTIFICATION_ID = 1;

  /**
   * Tell the OS and user we are now doing an important background operation
   * 
   * @param tickerMsg
   * @param notificationText
   */
  public void startForeground(String tickerMsg, String notificationText) {
    int icon = android.R.drawable.stat_sys_download;
    long when = System.currentTimeMillis();

    Notification notification = new Notification(icon, tickerMsg, when);

    // FIXME, use real
    Context context = getApplicationContext();
    CharSequence contentTitle = "Gaggle";
    CharSequence contentText = notificationText;
    Intent notificationIntent = new Intent(this, TopActivity.class);
    PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
        notificationIntent, 0);

    notification.setLatestEventInfo(context, contentTitle, contentText,
        contentIntent);

    startForegroundGlue(NOTIFICATION_ID, notification);
  }

  /**
   * Tell user are no longer running a critical foreground service
   */
  public void stopForeground() {
    stopForegroundGlue(true);
  }

  /**
   * This method doesn't exist on android 1.6, so we use reflection to fallback
   * intelligently
   * 
   * @param id
   * @param notification
   */
  private void startForegroundGlue(int id, Notification notification) {
    try {
      Method m = Service.class.getMethod("startForeground", new Class[] {
          Integer.TYPE, Notification.class });
      m.invoke(this, id, notification);
    } catch (NoSuchMethodException ex) {
      // Fall back to the old API
      // setForeground(true);
      try {
        Method m = Service.class.getMethod("setForeground",
            new Class[] { Boolean.TYPE });
        m.invoke(this, true);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * This method doesn't exist on android 1.6, so we use reflection to fallback
   * intelligently
   * 
   */
  private void stopForegroundGlue(boolean removeNotification) {
    try {
      Method m = Service.class.getMethod("stopForeground",
          new Class[] { Boolean.TYPE, });
      m.invoke(this, removeNotification);
    } catch (NoSuchMethodException ex) {
      // Fall back to the old API
      // setForeground(false);
      try {
        Method m = Service.class.getMethod("setForeground",
            new Class[] { Boolean.TYPE });
        m.invoke(this, false);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Used to request location/Status updates (once we have someone asking for
   * updates we will turn GPS on)
   * 
   * @param l
   *          the callback will be called from the background GPS thread
   * @param minTimeMs
   *          we prefer to see updates at least this often
   * @param minDistMeters
   *          if dist changes more than this wed like to find out
   * 
   */
  public void addLocationListener(long minTimeMs, float minDistMeters,
      LocationListener l) {
    // Log.d(TAG, "Add listener: " + l.toString());

    int oldcount;

    synchronized (listeners) {
      oldcount = listeners.size();

      listeners.put(l, new UpdateFreq(minTimeMs, (int) minDistMeters));
    }

    // FIXME, we are only shrinking the range of acceptable times - someday
    // we should
    // loosen things up
    boolean needUpdate = false;
    if (minTimeMs < minTimePerUpdate) {
      minTimePerUpdate = minTimeMs;
      needUpdate = true;
    }

    if (minDistMeters < minDistPerUpdate) {
      minDistPerUpdate = minDistMeters;
      needUpdate = true;
    }

    if (oldcount == 0 || needUpdate) { // We just added the first element -
      // subscribe to the OS
      String provider = (simData != null) ? simData.getProvider()
          : LocationManager.GPS_PROVIDER;

      manager.requestLocationUpdates(provider, minTimeMs, minDistMeters,
          listener, thread.getLooper());

      final GagglePrefs prefs = GagglePrefs.getInstance();


      if (prefs.useNmeaGeoidInfo()){
		  manager.addNmeaListener(nmea_listener);
      }
    }

    // Provide an initial location if we know where we are
    if (currentStatus == AVAILABLE) {
      Location loc = getLastKnownLocation();
      if (loc != null)
        l.onLocationChanged(loc);
    }
  }

  /**
   * We are no longer interested in updates
   * 
   * @param l
   */
  public void removeLocationListener(LocationListener l) {
    // Log.d(TAG, "Remove listener: " + l.toString());

    int newcount;
    synchronized (listeners) {
      listeners.remove(l);
      newcount = listeners.size();
    }

    if (newcount == 0) {
      manager.removeUpdates(listener);

      final GagglePrefs prefs = GagglePrefs.getInstance();

      if (prefs.useNmeaGeoidInfo()){
    	  manager.removeNmeaListener(nmea_listener);
      }
    }
  }

  public Location getLastKnownLocation() {
    String provider = (simData != null) ? simData.getProvider()
        : LocationManager.GPS_PROVIDER;
    return manager.getLastKnownLocation(provider);
  }

  private Double geoid_separation = null;
  
  private NmeaListener nmea_listener = new NmeaListener() {

	    // Used to avoid holding the lock while running (slow) handlers
//	    private ArrayList<LocationListener> lcopy = new ArrayList<LocationListener>();
	    
	    public void onNmeaReceived(long timestamp, String nmea) {
	        final GagglePrefs prefs = GagglePrefs.getInstance();

	    	if(prefs.debugAltitudeCorrection() && alti_debug_file != null){
	    		alti_debug_file.println("NMEA Timestamp is :" +timestamp+"   nmea is :"+nmea);
	    	}
    		Log.d(TAG,"Nmea Received :");
    		Log.d(TAG,"Timestamp is :" +timestamp+"   nmea is :"+nmea);
	    	String[] syl = nmea.split(",");
	    	try{
	    		if (syl[0].equals("$GPGGA")){
	    			geoid_separation = Double.parseDouble(syl[11]);
	    			final String unit = syl[12];
	    			if (unit.equals("F")){
	    				geoid_separation *= 0.3048;
	    			}
	    	    	if(prefs.debugAltitudeCorrection() && alti_debug_file != null){
	    	    		alti_debug_file.println("NMEA GEOID sep:" + geoid_separation);
	    	    	}
	    		}
	    	} catch(Exception e){
	    		Log.d(TAG, "Error parsing NMEA");
	    		geoid_separation = null;
	    	}
	    }};
  
  /**
   * We just rebroadcast our loc updates - but from inside our shared handler
   * thread
   * 
   */
  private LocationListener listener = new LocationListener() {

    // Used to avoid holding the lock while running (slow) handlers
    private ArrayList<LocationListener> lcopy = new ArrayList<LocationListener>();

    /**
     * Should we send an update to this listener?
     */
    private boolean isUpdate(Entry<LocationListener, UpdateFreq> e, Location loc) {
      long now = loc.getTime();
      UpdateFreq freq = e.getValue();

      // We don't want to deliver updates too often
      boolean doUpdate = (now - freq.lastUpdateTime >= freq.minTime);

      // FIXME - check distance

      if (doUpdate)
        freq.lastUpdateTime = now;

      return doUpdate;
    }

    @Override
    public synchronized void onLocationChanged(Location location) {

    	// http://code.google.com/p/android/issues/detail?id=23937
    	// Some Samsung phone have a bug that cause
    	// the date from GPS to be offset by 1 day
    	if (galaxys_leap_year_fix){
    		Log.d("GPSClient", "time before:" + location.getTime());
    		location.setTime(location.getTime() - 3600*24*1000);
    		Log.d("GPSClient", "time after:" + location.getTime());
    	}
      // If we receive a position update, assume the GPS is working
      currentStatus = AVAILABLE;
      final GagglePrefs prefs = GagglePrefs.getInstance();

      double correction = 0;

      if (prefs.useEGM84Geoid()){
    	  correction = geoid_correction.getGeoidSep(location.getLongitude(), location.getLatitude());
    	  if (prefs.debugAltitudeCorrection() && alti_debug_file != null){
    		  alti_debug_file.println("EGM84 correction for " + location.getLongitude() +
    				  ", " + location.getLatitude() + ":" + correction);
    	  }
      } else if (prefs.useNmeaGeoidInfo()){
    	  correction = geoid_separation != null ? geoid_separation : 0;
    	  if (prefs.debugAltitudeCorrection() && alti_debug_file != null){
    		  alti_debug_file.println("NMEA correction for " + location.getLongitude() +
    				  ", " + location.getLatitude() + ":" + correction);
    	  }
      }

      if (prefs.debugAltitudeCorrection() && alti_debug_file != null){
		  alti_debug_file.println("Original alti: " + location.getAltitude());
	  }
      if (correction != 0 && location.hasAltitude()){
    	  location.setAltitude(location.getAltitude() - correction);
      }
      if (prefs.debugAltitudeCorrection() && alti_debug_file != null){
		  alti_debug_file.println("Final alti: " + location.getAltitude());
	  }

      if (baro != null) {
        if (!hasSetBarometer && location.hasAltitude()) {
          hasSetBarometer = true;
          baro.setAltitude((float) location.getAltitude());
        }

        // Before forwarding the location to others, substitude the
        // (better) baro based altitude
        baro.improveLocation(location);
      }

      // Used to avoid holding the lock while running (slow) handlers
      synchronized (listeners) {
        // lcopy = listeners.keySet().toArray(lcopy);

        lcopy.clear();
        for (Entry<LocationListener, UpdateFreq> l : listeners.entrySet())
          if (isUpdate(l, location))
            lcopy.add(l.getKey());
      }

      for (LocationListener listen : lcopy)
        listen.onLocationChanged(location);
    }

    @Override
    public void onProviderDisabled(String provider) {
      synchronized (listeners) {
        for (LocationListener l : listeners.keySet())
          l.onProviderDisabled(provider);
      }
    }

    @Override
    public void onProviderEnabled(String provider) {
      synchronized (listeners) {
        for (LocationListener l : listeners.keySet())
          l.onProviderEnabled(provider);
      }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

      currentStatus = status;

      synchronized (listeners) {
        for (LocationListener l : listeners.keySet())
          l.onStatusChanged(provider, status, extras);
      }
    }
  };
}
