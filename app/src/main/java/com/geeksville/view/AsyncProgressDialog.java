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
package com.geeksville.view;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * An async task that takes care of showing a progress dialog and optionally put
 * up a message to show completion
 * 
 * @author kevinh
 * 
 *         We assume you pass in all the state needed to doInBackground by
 *         bundling it up in a closure, so no execute args are supported
 */
public abstract class AsyncProgressDialog extends AsyncTask<Void, Void, Void> {

	private static final String TAG = "AsyncProgressDialog";

	private String completionMessage = null;
	private String completionDialogTitle = null;
	private ProgressDialog dialog;

	protected Context context;

	public AsyncProgressDialog(Context _context, String dialogTitle, String dialogMessage) {
		context = _context;

		dialog = ProgressDialog.show(context, dialogTitle, dialogMessage, true, false);
	}

	/**
	 * 
	 * @return true if we will show a dialog
	 */
	protected boolean isShowingDialog() {
		return completionDialogTitle != null;
	}

	/**
	 * The message to show the user after an operation completes.
	 * 
	 * @param message
	 * 
	 *            Note: we show either the completion toast, or the completion
	 *            dialog, not both
	 */
	protected void showCompletionToast(String message) {
		completionMessage = message;
	}

	/**
	 * Show the user an alert informing them of the result for an operation
	 * 
	 * @param dialogTitle
	 * @param message
	 * 
	 *            Note: we show either the completion toast, or the completion
	 *            dialog, not both
	 */
	protected void showCompletionDialog(String dialogTitle, String message) {
		completionMessage = message;
		completionDialogTitle = dialogTitle;
	}

	protected void onPostExecute(Void unused) {
		Context context = dialog.getContext();

		try {
			dialog.dismiss();
		} catch (IllegalArgumentException ex) {
			Log.e(TAG, "Caught mystery: " + ex.getMessage());

			/*
			 * Handle the following auto bug report: I'm sorry, this Geeksville
			 * application has crashed. Would you please send this email so we
			 * can fix this bug? No personal information is being sent (you can
			 * check by reading the rest of the email).
			 * 
			 * com.geeksville.tracker generated the following exception:
			 * java.lang.IllegalArgumentException: View not attached to window
			 * manager
			 * 
			 * --------- Stack trace --------- 1.
			 * android.view.WindowManagerImpl.
			 * findViewLocked(WindowManagerImpl.java:355) 2.
			 * android.view.WindowManagerImpl
			 * .removeView(WindowManagerImpl.java:200) 3.
			 * android.view.Window$LocalWindowManager
			 * .removeView(Window.java:432) 4.
			 * android.app.Dialog.dismissDialog(Dialog.java:280) 5.
			 * android.app.Dialog.access$000(Dialog.java:73) 6.
			 * android.app.Dialog$1.run(Dialog.java:109) 7.
			 * android.app.Dialog.dismiss(Dialog.java:264) 8.
			 * com.geeksville.view
			 * .AsyncProgressDialog.onPostExecute(AsyncProgressDialog.java:65)
			 * 9.
			 * com.geeksville.tracker.SendGeoSMS$2.onPostExecute(SendGeoSMS.java
			 * :264) 10.com.geeksville.view.AsyncProgressDialog.onPostExecute(
			 * AsyncProgressDialog.java:1) 11.
			 * android.os.AsyncTask.finish(AsyncTask.java:417) 12.
			 * android.os.AsyncTask.access$300(AsyncTask.java:127) 13.
			 * android.os
			 * .AsyncTask$InternalHandler.handleMessage(AsyncTask.java:429) 14.
			 * android.os.Handler.dispatchMessage(Handler.java:99) 15.
			 * android.os.Looper.loop(Looper.java:123) 16.
			 * android.app.ActivityThread.main(ActivityThread.java:4338) 17.
			 * java.lang.reflect.Method.invokeNative(Native Method) 18.
			 * java.lang.reflect.Method.invoke(Method.java:521) 19.
			 * com.android.internal
			 * .os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:860) 20.
			 * com.android.internal.os.ZygoteInit.main(ZygoteInit.java:618) 21.
			 * dalvik.system.NativeStart.main(Native Method)
			 * -------------------------------
			 * 
			 * -------- Environment -------- Time =2010.03.30_16.25.42_CDT
			 * Device
			 * =verizon/voles/sholes/sholes:2.0.1/ESD56/20996:user/release-keys
			 * Make =Motorola Model =Droid Product =voles App
			 * =com.geeksville.tracker, version 1.05 (build 7) Locale=English
			 * (United States) -----------------------------
			 * 
			 * END REPORT.
			 */
		}

		if (isShowingDialog()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(completionDialogTitle);
			builder.setMessage(completionMessage);
			builder.setPositiveButton("Okay", null);

			AlertDialog alert = builder.create();
			alert.show();
		} else if (completionMessage != null)
			Toast.makeText(context, completionMessage, Toast.LENGTH_LONG).show();
	}

	/**
	 * Subclasses must provide an implementation
	 */
	protected abstract void doInBackground();

	@Override
	protected Void doInBackground(Void... params) {
		try {
			doInBackground();
		} catch (Exception ex) {
			// If our background thread threw an uncaught exception, convert it
			// into an error dialog/toast
			completionMessage = ex.getLocalizedMessage();
		}

		return null;
	}
}
