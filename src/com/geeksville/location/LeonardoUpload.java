/****************************************************************************************
 * Gaggle is Copyright 2010, 2011, and 2012 by Kevin Hester of Geeksville Industries LLC,
 * a California limited liability corporation. 
 * 
 * Gaggle is free software: you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation, either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * Gaggle is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE.  See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with Gaggle 
 * included in this distribution in the manual (assets/manual/gpl-v3.txt). If not, see  
 * <http://www.gnu.org/licenses/> or at <http://gplv3.fsf.org>.
 ****************************************************************************************/
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
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
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
			int competitionClass, String shortFilename, String igcFile, int connectionTimeout, int operationTimeout)
			throws IOException {

		// Strip off extension (leonado docs say they don't want it
		int i = shortFilename.lastIndexOf('.');
		if (i >= 1)
			shortFilename = shortFilename.substring(0, i);
		String sCompetitionClass = String.valueOf(competitionClass);
		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		HttpConnectionParams.setConnectionTimeout(httpParameters, connectionTimeout);
		// Set the default socket timeout (SO_TIMEOUT) 
		// in milliseconds which is the timeout for waiting for data.
		HttpConnectionParams.setSoTimeout(httpParameters, operationTimeout);

		HttpClient httpclient = new DefaultHttpClient(httpParameters);
		httpclient.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE,
				false);
		HttpPost httppost = new HttpPost(postURL);
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("user", username));
		nameValuePairs.add(new BasicNameValuePair("pass", password));
		nameValuePairs.add(new BasicNameValuePair("igcfn", shortFilename));
		nameValuePairs.add(new BasicNameValuePair("Klasse", sCompetitionClass));
		nameValuePairs.add(new BasicNameValuePair("IGCigcIGC", igcFile));
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
			int bodLoc = resp.indexOf("<body>");
			if (bodLoc >= 0)
				resp = resp.substring(bodLoc + 6);
			int probLoc = resp.indexOf("problem");
			if (probLoc >= 0)
				resp = resp.substring(probLoc + 7);
			if (resp.startsWith("<br>"))
				resp = resp.substring(4); 
			int markLoc = resp.indexOf('<');
			if (markLoc >= 0)
				resp = resp.substring(0, markLoc);
			resp = resp.trim();
		}

		return resp;
	}
}