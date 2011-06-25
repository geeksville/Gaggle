/**
 * 
 */
package com.geeksville.location;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
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

	private static GPSClient instance;

	private HashMap<LocationListener, UpdateFreq> listeners = new HashMap<LocationListener, UpdateFreq>();

	private long minTimePerUpdate = Long.MAX_VALUE;

	private float minDistPerUpdate = Float.MAX_VALUE;

	/**
	 * In developer mode, this provides our data
	 */
	private TestGPSDriver simData = null;

	private int currentStatus = OUT_OF_SERVICE;

	/**
	 * Older SDKs don't define LocationProvider.AVAILABLE etc...
	 */
	private static final int AVAILABLE = 2, OUT_OF_SERVICE = 0,
			TEMPORARILY_UNAVAILABLE = 1;

	private static Map<Context, Republisher> clients = new HashMap<Context, Republisher>();

	public GPSClient() {
	}

	/**
	 * Glue to bind to this service
	 * 
	 * @param context
	 *            the client's context
	 * @param client
	 *            who to notify
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
			context.bindService(new Intent(context, GPSClient.class),
					conns, Service.BIND_AUTO_CREATE
							| Service.BIND_DEBUG_UNBIND);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();

		FlurryAgent.onStartSession(this, "XBPNNCR4T72PEBX17GKF");

		instance = this;
		manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// Start our looper up
		thread.start();

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
					Toast t = Toast
							.makeText(
									this,
									R.string.mock_location_required,
									Toast.LENGTH_LONG);
					t.show();

					Editor e = PreferenceManager.getDefaultSharedPreferences(
							this).edit();
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

		// Forcibly unsubscribe anyone still using us
		synchronized (listeners) {
			if (listeners.size() != 0) {
				LocationListener[] tokill = listeners.keySet().toArray(null);
				for (LocationListener l : tokill)
					removeLocationListener(l);

				listeners.clear();
			}
		}

		thread.getLooper().quit();

		FlurryAgent.onEndSession(this);

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
	 * This method doesn't exist on android 1.6, so we use reflection to
	 * fallback intelligently
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
	 * This method doesn't exist on android 1.6, so we use reflection to
	 * fallback intelligently
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
	 *            the callback will be called from the background GPS thread
	 * @param minTimeMs
	 *            we prefer to see updates at least this often
	 * @param minDistMeters
	 *            if dist changes more than this wed like to find out
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

			manager.requestLocationUpdates(provider, minTimePerUpdate,
					minDistPerUpdate, listener, thread.getLooper());
		}

		// Provide an initial location if we know where we are
		if (currentStatus == AVAILABLE) {
			Location loc = getLastKnownLocation();
			if (loc != null)
				l.onLocationChanged(loc);
		}
	}

	/**
	 * We are no long interested in updates
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
		}
	}

	public Location getLastKnownLocation() {
		String provider = (simData != null) ? simData.getProvider()
				: LocationManager.GPS_PROVIDER;

		return manager.getLastKnownLocation(provider);
	}

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
		private boolean isUpdate(Entry<LocationListener, UpdateFreq> e,
				Location loc) {
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

			// If we receive a position update, assume the GPS is working
			currentStatus = AVAILABLE;

			// Used to avoid holding the lock while running (slow) handlers
			synchronized (listeners) {
				// lcopy = listeners.keySet().toArray(lcopy);

				lcopy.clear();
				for (Entry<LocationListener, UpdateFreq> l : listeners
						.entrySet())
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
