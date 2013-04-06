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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import com.geeksville.gaggle.R;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Base64;
import android.util.Log;

import com.geeksville.io.LineEndingStream;

/**
 * 
 * @author kevinh
 * 
 *         Writes IGC files (FAI gliding competition GPS data) Sample file
 *         follows:
 * 
 * 
 *         // name convention: 2009-12-25-XXX-SERN-YY.IGC // where XXX is the
 *         mfgr code. I'll pick GEK, YY is flight num for that day 01, etc... //
 *         if not paying fee I should use XXX
 * 
 *         // Contents // CR/LF at end of each line
 */
public class IGCWriter implements PositionWriter {
	private PrintStream out;
	private Signature sig;

	private boolean didProlog = false;
	private final String versionString;

	private String pilotName;
	private String flightDesc;
	private String gliderType;
	private String pilotId;
	private boolean hasJRecord = false;

	private Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

	public class SignatureOutputStream extends OutputStream {

		private OutputStream target;
		private Signature sig;

		/**
		 * creates a new SignatureOutputStream which writes to
		 * a target OutputStream and updates the Signature object.
		 */
		public SignatureOutputStream(OutputStream target, Signature sig) {
			this.target = target;
			this.sig = sig;
		}

		@Override
		public void write(int b) throws IOException {
			write(new byte[] { (byte) b });
		}

		@Override
		public void write(byte[] b) throws IOException {
			write(b, 0, b.length);
		}

		@Override
		public void write(byte[] b, int offset, int len) throws IOException {
			target.write(b, offset, len);
			try {
				for (int i = 0; i < len; i++)
					if (b[offset+i] != '\r' && b[offset+i] != '\n')
						sig.update(b, offset + i, 1);
			} catch (SignatureException ex) {
				throw new IOException(ex);
			}
		}

		@Override
		public void flush() throws IOException {
			target.flush();
		}

		@Override
		public void close() throws IOException {
			target.close();
		}
	}

	private PrivateKey getPrivateKey(Context context) throws NoSuchAlgorithmException, InvalidKeySpecException {
		final String private_key = context.getString(R.string.igc_private_key);
		KeyFactory fac = KeyFactory.getInstance("RSA");
		EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(Base64.decode(private_key, Base64.DEFAULT));
		return fac.generatePrivate(privKeySpec);
	}

	public IGCWriter(OutputStream dest, String pilotName, String flightDesc,
			String gliderType, String pilotId, Context context) throws IOException {
		String tmp_version;
		try {
			tmp_version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e1) {
			tmp_version = "UNKNOWN";
		}
		versionString = tmp_version;

		try {
			sig = Signature.getInstance("SHA1withRSA");
			PrivateKey pk = getPrivateKey(context);
			sig.initSign(pk);
			OutputStream dOut = new PrintStream(new LineEndingStream(new SignatureOutputStream(dest, sig)));
			out = new PrintStream(dOut);
		} catch (NoSuchAlgorithmException e) {
			Log.e("IGCWriter", "No such algo");
			out = new PrintStream(new LineEndingStream(dest));
		} catch (InvalidKeyException e) {
			Log.e("IGCWriter", "Invalid key");
			out = new PrintStream(new LineEndingStream(dest));
		} catch (InvalidKeySpecException e) {
			Log.e("IGCWriter", "Invalid key spec");
			out = new PrintStream(new LineEndingStream(dest));
		}

		this.gliderType = gliderType;
		this.pilotId = pilotId;
		this.pilotName = pilotName;
		this.flightDesc = flightDesc;
	}

	/**
	 * We close the output stream in the epilog
	 */
	@Override
	public void emitEpilog() {
		// sect 3.2, G=security record
		try {
			final byte[] signature = sig.sign();
			final String sigStr = Base64.encodeToString(signature, Base64.DEFAULT).replaceAll("[\\r\\n]", "");

			for (int i=0; i < sigStr.length() / 75 ; i++){
				out.println("G" + sigStr.substring(i*75, i*75+75));
			}
			if (sigStr.length() % 75 > 0){
				out.println("G" + sigStr.substring(((int)(sigStr.length() / 75))*75));
			}
		} catch (SignatureException e) {
			Log.e("IGCWriter", "Error when signing...", e);
			out.println("GGaggleFailedToSign");
		}
		out.close();
	}

	/**
	 * Return a degress in IGC format
	 * 
	 * @param degIn
	 * @return
	 */
	private static String degreeStr(double degIn, boolean isLatitude) {
		boolean isPos = degIn >= 0;
		char dirLetter = isLatitude ? (isPos ? 'N' : 'S') : (isPos ? 'E' : 'W');

		degIn = Math.abs(degIn);
		double minutes = 60 * (degIn - Math.floor(degIn));
		degIn = Math.floor(degIn);
		int minwhole = (int) minutes;
		int minfract = (int) ((minutes - minwhole) * 1000);

		// DDMMmmmN(or S) latitude
		// DDDMMmmmE(or W) longitude
		String s = String.format(Locale.US, (isLatitude ? "%02d" : "%03d")
				+ "%02d%03d%c", (int) degIn, minwhole, minfract, dirLetter);
		return s;
	}

	/**
	 * 
	 * @param time
	 *            UTC time of this fix, in milliseconds since January 1, 1970.
	 * @param latitude
	 * @param longitude
	 * 
	 *            sect 4.1, B=fix plus extension data mentioned in I
	 */
	@Override
	public void emitPosition(long time, double latitude, double longitude,
			float altitude, int bearing, float groundSpeed, float[] accel,
			float vspd) {
		// B
		// HHMMSS - time UTC
		// DDMMmmmN(or S) latitude
		// DDDMMmmmE(or W) longitude
		// A (3d valid) or V (2d only)
		// PPPPP pressure altitude (00697 in this case)
		// GGGGG alt above WGS ellipsode (00705 in this case)
		// GSP is 000 here (ground speed in km/hr)
		// B1851353728534N12151678WA0069700705000

		// Get time in UTC
		cal.setTimeInMillis(time);

		boolean is3D = !Double.isNaN(altitude);

		// Spit out our prolog if need be
		if (!didProlog) {
			emitProlog(cal);
			didProlog = true;
		}

		int hours = cal.get(Calendar.HOUR_OF_DAY);
		out.format(Locale.US, "B%02d%02d%02d%s%s%c%05d%05d%03d", hours, cal
				.get(Calendar.MINUTE), cal.get(Calendar.SECOND),
				degreeStr(latitude, true), degreeStr(longitude, false),
				is3D ? 'A' : 'V', (int) (is3D ? altitude : 0), // FIXME convert
																// altitudes
				// correctly
				(int) (is3D ? altitude : 0), // FIXME convert alts
				(int) groundSpeed);
		out.println();

		// Don't store vertical speed info until I can find an example data
		// file.
		if (!Float.isNaN(vspd) && false) {

			if (!hasJRecord) {
				// less frequent extension - vario data
				out.println("J010812VAR");
				hasJRecord = true;
			}

			out.format(Locale.US, "K%02d%02d%02d%03d", hours,
					cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND),
					(int) vspd * 10);
			out.println();
		}
	}

	/**
	 * Do the heavy lifting necessary to spit out a file header
	 */
	private void emitProlog(Calendar cal) {

		out.println("AXGG"+versionString); // AFLY06122 - sect 3.1, A=mfgr info,
		// mfgr=FLY, serial num=06122

		// sect 3.3.1, H=file header
		String dstr = String.format(Locale.US, "HFDTE%02d%02d%02d",
				cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1,
				(cal.get(Calendar.YEAR) - 1900) % 100); // date

		out.println(dstr); // date

		out.println("HFFXA100"); // accuracy in meters - required
		out.println("HFPLTPILOT:" + pilotName); // pilot (required)
		out.println("HFGTYGLIDERTYPE:" + gliderType); // glider type (required)
		out.println("HFGIDGLIDERID:" + pilotId); // glider ID required
		out.println("HFDTM100GPSDATUM:WGS84"); // datum required - must be wgs84
		out.println("HFGPSGPS:" + android.os.Build.MODEL); // info on gps
		// manufacturer
		out.println("HFRFWFIRMWAREVERSION:" + versionString); // sw version of app
		out.println("HFRHWHARDWAREVERSION:" + versionString); // hw version
		out.println("HFFTYFRTYPE:Geeksville,Gaggle"); // required: manufacturer
		// (me) and model num

		// sect 3.4, I=fix extension list
		out.println("I013638GSP"); // one extension, starts at byte 36, ends at
		// 38, extension type is ground speed (was TAS)
	}

	/**
	 * Add standard IGC prologue
	 * 
	 */
	@Override
	public void emitProlog() {
	}

}
