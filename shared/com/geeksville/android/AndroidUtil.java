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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

public class AndroidUtil {
	/**
	 * 
	 * @return True if we are running on the emulator
	 */
	public static boolean isEmulator() {
		return Build.MODEL.equals("google_sdk");
	}

	static private boolean hasContactsContract;

	static private boolean hasNewSmsAPI;

	static {
		try {
			Class.forName("android.provider.ContactsContract");
			hasContactsContract = true;
		} catch (Exception ex) {
			// ignore
		}

		try {
			Class.forName("android.telephony.SmsMessage");
			hasNewSmsAPI = true;
		} catch (Exception ex) {
			// ignore
		}

	}

	/**
	 * is the ContactsContract API implemented?
	 * 
	 * @return
	 */
	public static boolean isContactsContractAvailable() {
		return hasContactsContract;
	}

	/**
	 * is the the new (post GSM only) API implemented?
	 * 
	 * @return
	 */
	public static boolean isNewSmsAPI() {
		return hasNewSmsAPI;
	}

	/**
	 * Generate an input stream reading from an android URI
	 * 
	 * @param uri
	 * @return
	 * @throws IOException
	 */
	public static InputStream getFromURI(Context context, Uri uri) throws IOException {

		if (uri.getScheme().equals("content"))
			return context.getContentResolver().openInputStream(uri);
		else if (uri.getScheme().equals("file")) {
			URL url = new URL(uri.toString());

			return url.openStream();
		} else {
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(uri.toString());
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null)
				return entity.getContent();
			else
				throw new IOException("No HTTP response");
			// Use the regular java stuff
			// URL url = new URL(uri.toString());

			// return url.openStream();
		}
	}

	/**
	 * Get a stream that can be used for serialization
	 * 
	 * @param context
	 *            We'll write our data to this context private dir
	 * @param name
	 * @return
	 * @throws IOException
	 */
	public static ObjectOutputStream writeObjectStream(Context context, String name)
			throws IOException {
		OutputStream s = context.openFileOutput(name, Context.MODE_PRIVATE);

		// I'd prefer to not overwrite the old file, but Context doesn't offer a
		// fileRename option
		return new ObjectOutputStream(s);
	}

	public static ObjectInputStream readObjectStream(Context context, String name)
			throws StreamCorruptedException,
			IOException {
		InputStream s = context.openFileInput(name);

		// FIXME - add buffering?
		return new ObjectInputStream(s);
	}
}
