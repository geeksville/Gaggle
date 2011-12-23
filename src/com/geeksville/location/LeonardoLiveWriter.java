/**
 * 
 */
package com.geeksville.location;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.SystemClock;
import android.util.Log;

import com.flurry.android.FlurryAgent;

/**
 * Writes tracks to the Paragliding (or other) Lenoardo Live server
 * 
 * @author kevinh
 * 
 *         For a spec see:
 *         http://www.livetrack24.com/wiki/index.php?title=Leonardo
 *         %20Live%20Tracking%20API&mode=print&lang=en Supposedly they also have
 *         a UDP based API, which we should probably switch to at some point.
 * 
 *         FIXME - validate user input to quote & etc...
 */
public class LeonardoLiveWriter implements PositionWriter {

	/**
	 * Sent to server
	 */
	private String ourVersion;

	/**
	 * How many secs between position reports - FIXME, choose correctly
	 */
	private int expectedIntervalSecs;

	/**
	 * FIXME, get the real phone name
	 */
	private String phoneName = android.os.Build.MODEL;

	/**
	 * Our app name, FIXME - pick a good name
	 */
	private String programName = "Android Gaggle";

	/**
	 * FIXME - pass in from app
	 */
	private String vehicleName;

	/**
	 * Hardwired for paraglider - FIXME
	 */
	private int vehicleType = 1;

	/**
	 * Claim built in for now
	 */
	private String gpsType = "Internal GPS";

	private String userName;
	private String password;

	private String trackURL, clientURL;

	private int sessionId = (new Random()).nextInt(0x7fffffff);

	private int packetNum = 1;

	/**
	 * Constructor
	 * 
	 * @param serverURL
	 *            The server we are using, www.livetrack24.com/track.php for
	 *            real server, test.livetrack24.com for test
	 * @param userName
	 *            The user login name on the site (low security)
	 * @param password
	 *            The user password
	 * @throws Exception
	 */
	public LeonardoLiveWriter(Context context, String serverURL, String userName, String password,
			String vehicleName, int vehicleType, int expectedInterval) throws Exception {
		PackageManager pm = context.getPackageManager();
		PackageInfo pi;
		try {
			pi = pm.getPackageInfo(context.getPackageName(), 0);

			ourVersion = pi.versionName;
		} catch (NameNotFoundException eNnf) {
			throw new RuntimeException(eNnf); // We better be able to find the
			// info about our own package
		}

		URL url = new URL(serverURL + "/track.php");
		trackURL = url.toString();
		url = new URL(serverURL + "/client.php");
		clientURL = url.toString();

		this.userName = userName;
		this.password = password;
		this.vehicleType = vehicleType;
		this.vehicleName = vehicleName;
		expectedIntervalSecs = expectedInterval;

		doLogin(); // Login here, so we can find out about bad passwords ASAP
	}

	static int PACKET_START = 2; // FIXME, lookup java const syntax
	static int PACKET_END = 3;
	static int PACKET_POINT = 4;

	/**
	 * Cleans up illegal chars in a URL
	 * 
	 * @param url
	 * @return FIXME move
	 */
	static String normalizeURL(String url) {
		return url.replace(' ', '+'); // FIXME, do a better job of this
	}

	/**
	 * send the packet up to the server
	 * 
	 * @param packetType
	 *            Used to construct leolive code
	 * @param options
	 * @throws IOException
	 */
	void sendPacket(int packetType, String options) throws IOException {
		try {
			String urlstr = String.format(Locale.US,
					"%s?leolive=%d&sid=%d&pid=%d&%s", trackURL, packetType,
					sessionId, packetNum, options);

			URL url = new URL(normalizeURL(urlstr));

			url.openStream().close();
		} catch (MalformedURLException ex) {
			// We should have caught this in the constructor
			throw new RuntimeException(ex);
		}

		packetNum++;
	}

	/**
	 * @see com.geeksville.location.PositionWriter#emitEpilog()
	 */
	@Override
	public void emitEpilog() {
		try {
			// FIXME - add support for end of track types (need retrieve etc...)
			sendPacket(PACKET_END, "prid=0");
		} catch (IOException ex) {
			System.out.println("Ignoring on epilog: " + ex);
		}
	}
	long lastUpdateTime = SystemClock.elapsedRealtime();
	/**
	 * @see com.geeksville.location.PositionWriter#emitPosition(long, double,
	 *      double, float, int, float, float[])
	 */
	@Override
	public void emitPosition(long time, double latitude, double longitude, float altitude,
			int bearing, float groundSpeed, float[] accel) {
		try {
			int groundKmPerHr = (int) groundSpeed;
			int unixTimestamp = (int) (time / 1000); // Convert from msecs to
			// secs
			long now = SystemClock.elapsedRealtime();
			if (lastUpdateTime + (expectedIntervalSecs *1000) < now)
			{
				String opts = String.format(Locale.US,
						"lat=%f&lon=%f&alt=%d&sog=%d&cog=%d&tm=%d", latitude,
						longitude, Float.isNaN(altitude) ? 0 : (int) altitude, groundKmPerHr, bearing,
						unixTimestamp);
				Log.d("XXX", opts);
				sendPacket(PACKET_POINT, opts);
				lastUpdateTime = SystemClock.elapsedRealtime();
			}
		} catch (IOException ex) {
			System.out.println("Ignoring on epilog: " + ex);
		}
	}

	private void doLogin() throws Exception {
		// If the user has an account on the server then the sessionID must be
		// constructed in the following way to contain the userID
		// First of all your application must get the userID based on the
		// username and password of the user. The url to verify user accounts
		// and get back the userID is
		// http://www.livetrack24.com/client.php?op=login&user=username&pass=pass
		// The username and password are case INSENSITIVE, because on mobile
		// devices it is not easy for all users to enter the correct case.
		// The result of the page is an integer, 0 if userdata are incorrect, or
		// else the userID of the user
		String urlstr = String.format("%s?op=login&user=%s&pass=%s", clientURL, userName, password);

		URL url = new URL(normalizeURL(urlstr));

		InputStream responseStream = url.openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
		String response = reader.readLine();

		try {
			int userID = Integer.parseInt(response);

			if (userID == 0)
				throw new Exception("Invalid username or password");

			Random a = new Random(System.currentTimeMillis());
			int rnd = Math.abs(a.nextInt());
			// we make an int with leftmost bit=1 ,
			// the next 7 bits random
			// (so that the same userID can have multiple active sessions)
			// and the next 3 bytes the userID
			sessionId = (rnd & 0x7F000000) | (userID & 0x00ffffff) | 0x80000000;
		} catch (NumberFormatException ex) {
			throw new Exception("Unexpected server response");
		}
	}

	/**
	 * @see com.geeksville.location.PositionWriter#emitProlog(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public void emitProlog() {
		try {
			String opts = String
					.format(
							Locale.US,
							"client=%s&v=%s&user=%s&pass=%s&phone=%s&gps=%s&trk1=%d&vtype=%d&vname=%s",
							programName, ourVersion, userName, password, phoneName, gpsType,
							expectedIntervalSecs, vehicleType, vehicleName);
			Log.d("XXX", opts);
			sendPacket(PACKET_START, opts);

			// Keep stats on # of live uploads
			Map<String, String> map = new HashMap<String, String>();
			map.put("User", userName);
			map.put("Time", (new Date()).toGMTString());
			FlurryAgent.onEvent("LiveUpload", map);

		} catch (IOException ex) {
			System.out.println("FIXME, rethrow on connect failed " + ex);
		}
	}

}
