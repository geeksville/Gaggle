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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

/**
 * Send exception reports via email
 * 
 * @author kevinh
 * 
 */
public class EmailExceptionHandler extends PostMortemReportExceptionHandler implements Runnable {

	public static final String ExceptionReportFilename = "postmortem.trace";

	private static final String MSG_SUBJECT_TAG = "Exception Report"; // "app title + this tag"
	// =
	// email
	// subject
	private static final String MSG_SENDTO = "kevinh@geeksville.com"; // email
	// will
	// be sent
	// to this
	// account
	// the following may be something you wish to consider localizing
	private String MSG_BODY = "I'm sorry, this Geeksville application has crashed.  Would you please send this email so we can fix this bug? "
			+
			"No personal information is being sent (you can check by reading the rest of the email).";

	public EmailExceptionHandler(Activity aApp) {
		super(aApp);
	}

	public Boolean sendDebugReportToAuthor(String aReport) {
		if (aReport != null) {
			Intent theIntent = new Intent(Intent.ACTION_SEND);
			String theSubject = mApp.getTitle() + " " + MSG_SUBJECT_TAG;
			String theBody = "\n" + MSG_BODY + "\n\n" + aReport + "\n\n";
			theIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { MSG_SENDTO });
			theIntent.putExtra(Intent.EXTRA_TEXT, theBody);
			theIntent.putExtra(Intent.EXTRA_SUBJECT, theSubject);
			theIntent.setType("message/rfc822");
			Boolean hasSendRecipients = (mApp.getPackageManager().queryIntentActivities(theIntent,
					0).size() > 0);
			if (hasSendRecipients) {
				mApp.startActivity(theIntent);
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	protected void saveDebugReport(String aReport) {
		// save report to file
		try {
			FileOutputStream theFile = mApp.openFileOutput(ExceptionReportFilename,
					Context.MODE_PRIVATE);
			theFile.write(aReport.getBytes());
			theFile.close();
		} catch (IOException ioe) {
			// error during error report needs to be ignored, do not wish to
			// start infinite loop
		}
	}

	public void sendDebugReportToAuthor() {
		String theLine = "";
		String theTrace = "";
		try {
			BufferedReader theReader = new BufferedReader(
					new InputStreamReader(mApp.openFileInput(ExceptionReportFilename)));
			while ((theLine = theReader.readLine()) != null) {
				theTrace += theLine + "\n";
			}
			if (sendDebugReportToAuthor(theTrace)) {
				mApp.deleteFile(ExceptionReportFilename);
			}
		} catch (FileNotFoundException eFnf) {
			// nothing to do
		} catch (IOException eIo) {
			// not going to report
		}
	}

	public void run() {
		sendDebugReportToAuthor();
	}

	@Override
	protected void submit(Throwable e) {
		String theErrReport = getDebugReport(e);
		saveDebugReport(theErrReport);
		// try to send file contents via email (need to do so via the UI thread)
		mApp.runOnUiThread(this);
	}
}
