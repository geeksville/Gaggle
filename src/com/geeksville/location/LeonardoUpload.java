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
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

/**
 * Uses an HTTP post to submit an IGC file to Leonardo
 * 
 * @author kevinh Per
 *         http://www.paraglidingforum.com/leonardo/doc/direct_upload.html
 */
public class LeonardoUpload {

	/**
	 * Upload a flight to Leonardo
	 * 
	 * @param username
	 * @param password
	 * @param postURL
	 * @param shortFilename
	 * @param igcFile
	 *            we will take care of closing this stram
	 * @return null for success, otherwise a string description of the problem
	 * @throws IOException
	 */
	public static String upload(String username, String password, String postURL,
			String shortFilename, String igcFile)
			throws IOException {

		// Strip off extension (leonado docs say they don't want it
		int i = shortFilename.lastIndexOf('.');
		if (i >= 1)
			shortFilename = shortFilename.substring(0, i);

		HttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE,
				false);
		HttpPost httppost = new HttpPost(postURL);

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user", username));
		nameValuePairs.add(new BasicNameValuePair("pass", password));
		nameValuePairs.add(new BasicNameValuePair("igcfn", shortFilename));
		nameValuePairs.add(new BasicNameValuePair("Klasse", "3"));
		nameValuePairs.add(new BasicNameValuePair("IGCigcIGC", igcFile));
		// FIXME,for now we always claim paraglider, open
		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		HttpResponse response = httpclient.execute(httppost);
		HttpEntity entity = response.getEntity();

		String resp = EntityUtils.toString(entity);

		// An error looks like:
		// <html><body>problem<br>This is not a valid .igc
		// file</body></html>

		// Check for success
		if (resp.contains("flight scored"))
			resp = null;
		else {
			// Strip off body
			int bodLoc = resp.indexOf("<body>");
			if (bodLoc >= 0)
				resp = resp.substring(bodLoc + 6);

			int probLoc = resp.indexOf("problem");
			if (probLoc >= 0)
				// drop problem
				resp = resp.substring(probLoc + 7);

			if (resp.startsWith("<br>"))
				resp = resp.substring(4); // drop br

			// Drop any other markup
			int markLoc = resp.indexOf('<');
			if (markLoc >= 0)
				resp = resp.substring(0, markLoc);
		}

		return resp;
	}
}
