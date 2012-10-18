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
package com.geeksville.android;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

public abstract class PostMortemReportExceptionHandler implements UncaughtExceptionHandler {

	private Thread.UncaughtExceptionHandler mDefaultUEH;
	protected Activity mApp = null;

	protected PostMortemReportExceptionHandler(Activity aApp) {
		mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
		mApp = aApp;
	}

	public void perhapsInstall() {
		int androidVer = Integer.parseInt(Build.VERSION.SDK); // SDK_INT not on
																// 1.5

		// On android 2.2 or later use google's exception reporting instead
		if (androidVer < 8)
			Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		submit(e);
		// do not forget to pass this exception through up the chain
		mDefaultUEH.uncaughtException(t, e);
	}

	public String getDebugReport(Throwable aException) {
		NumberFormat theFormatter = new DecimalFormat("#0.");
		String theErrReport = "";

		theErrReport += mApp.getPackageName() + " generated the following exception:\n";
		theErrReport += aException.toString() + "\n\n";

		// stack trace
		StackTraceElement[] theStackTrace = aException.getStackTrace();
		if (theStackTrace.length > 0) {
			theErrReport += "--------- Stack trace ---------\n";
			for (int i = 0; i < theStackTrace.length; i++) {
				theErrReport += theFormatter.format(i + 1) + "\t" + theStackTrace[i].toString()
						+ "\n";
			}// for
			theErrReport += "-------------------------------\n\n";
		}

		// if the exception was thrown in a background thread inside
		// AsyncTask, then the actual exception can be found with getCause
		Throwable theCause = aException.getCause();
		if (theCause != null) {
			theErrReport += "----------- Cause -----------\n";
			theErrReport += theCause.toString() + "\n\n";
			theStackTrace = theCause.getStackTrace();
			for (int i = 0; i < theStackTrace.length; i++) {
				theErrReport += theFormatter.format(i + 1) + "\t" + theStackTrace[i].toString()
						+ "\n";
			}// for
			theErrReport += "-----------------------------\n\n";
		}// if

		// app environment
		PackageManager pm = mApp.getPackageManager();
		PackageInfo pi;
		try {
			pi = pm.getPackageInfo(mApp.getPackageName(), 0);
		} catch (NameNotFoundException eNnf) {
			// doubt this will ever run since we want info about our own package
			pi = new PackageInfo();
			pi.versionName = "unknown";
			pi.versionCode = 69;
		}
		Date theDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss_zzz");
		theErrReport += "-------- Environment --------\n";
		theErrReport += "Time\t=" + sdf.format(theDate) + "\n";
		theErrReport += "Device\t=" + Build.FINGERPRINT + "\n";
		try {
			Field theMfrField = Build.class.getField("MANUFACTURER");
			theErrReport += "Make\t=" + theMfrField.get(null) + "\n";
		} catch (SecurityException e) {
		} catch (NoSuchFieldException e) {
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		}
		theErrReport += "Model\t=" + Build.MODEL + "\n";
		theErrReport += "Product\t=" + Build.PRODUCT + "\n";
		theErrReport += "App\t\t=" + mApp.getPackageName() + ", version " + pi.versionName
				+ " (build " + pi.versionCode + ")\n";
		theErrReport += "Locale=" + mApp.getResources().getConfiguration().locale.getDisplayName()
				+ "\n";
		theErrReport += "-----------------------------\n\n";

		theErrReport += "END REPORT.";
		return theErrReport;
	}

	protected abstract void submit(Throwable e);
}
