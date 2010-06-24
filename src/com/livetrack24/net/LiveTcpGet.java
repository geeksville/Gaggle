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
package com.livetrack24.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

public class LiveTcpGet implements Runnable {
	private static final String TAG = "LiveTcpGet";

	final static int ERR_NETWORK_IS_DISABLED = 1;
	final static int ERR_NETWORK_ERROR = 2;
	final static int ERR_SERVER_TIMEOUT = 3;
	final static int ERR_BAD_REQUEST = 4;
	final static int ERR_PROXY_URL_NOT_RESPONDING = 10;
	final static int ERR_GENERIC_ERROR = 99;

	private Thread mThread = null;

	// for tcp streams
	public StreamConnection streamConnection = null;
	public OutputStream outputStream = null;
	public DataOutputStream dataOutputStream = null;

	// use InputStream to receive responses from Web server
	public InputStream inputStream = null;
	public DataInputStream dataInputStream = null;

	private String serverToUse;
	private byte[] requestBytes;

	private boolean finished = false;
	private int resultCode = -1;
	private String result = "";

	public boolean onlySend = false;

	public LiveTcpGet() {
		mThread = null;
	}

	public void open(String server, byte[] pRequestBytes) {

		close();

		requestBytes = pRequestBytes;
		serverToUse = server;
		mThread = new Thread(this);
		mThread.start();
	}

	public void open(String server, String pRequestString) {
		open(server, pRequestString.getBytes());
	}

	public void close() {

		if (mThread != null) {
			Thread thread = mThread;
			mThread = null;
			try {
				thread.join();
			} catch (InterruptedException ex) {
				Log.e(TAG, "LiveTcpGet.close: Exception: " + ex.getMessage());
			}
		}

		close_TCP_Conn();

	}

	public boolean isFinished() {
		return finished;
	}

	public int getResultCode() {
		return resultCode;
	}

	public String getResult() {
		return result;
	}

	public void run() {

		try {
			makeRequest(requestBytes);
			return;
		} catch (Exception ex1) {
			Log.e(TAG, "LiveTcpGet.run: Exception 1:" + ex1.getMessage());
		}

	}

	// --------------------------------------------------------------------
	// --------------------------------------------------------------------
	// --------------------------------------------------------------------

	public boolean open_TCP_Conn() {

		if (dataOutputStream != null || outputStream != null || streamConnection != null) {
			Log.e(TAG, "LiveTcpGet.open_TCP_Conn : TCP is already open");
			close_TCP_Conn();
			/*
			 * finished=true; resultCode=ERR_GENERIC_ERROR; result=""; return
			 * false;
			 */
		}

		if (false) {
			streamConnection = null;
			// use OutputStream to send requests
			outputStream = null;
			dataOutputStream = null;
		}

		// specify the connect string
		String connectString = "socket://" + serverToUse + ":" + sPreferences.getPortNum();
		Log.e(TAG, "LiveTcpGet: Opening TCP Connection at " + connectString);

		try {
			// establish a socket connection with remote server
			streamConnection = (StreamConnection) Connector.open(connectString);
		} catch (IOException e) {
			Log.e(TAG, "Exception # 1 LiveTcpGet.open_TCP_Conn:" + e.getMessage());
			finished = true;
			resultCode = ERR_GENERIC_ERROR;
			result = "";
			return false;
		}

		try {
			// create DataOuputStream on top of the socket connection
			outputStream = streamConnection.openOutputStream();
			dataOutputStream = new DataOutputStream(outputStream);

		} catch (IOException e) {
			Log.e(TAG, "Exception # 2 LiveTcpGet.open_TCP_Conn:" + e.getMessage());
			finished = true;
			resultCode = ERR_GENERIC_ERROR;
			result = "";
			return false;
		}

		return true;
	}

	public void close_TCP_Conn() {

		Log.e(TAG, "LiveTcpGet Closing TCP connection");
		try {
			if (dataOutputStream != null)
				dataOutputStream.close();
		} catch (Exception e1) {
			Log.e(TAG, "Exception LiveTcpGet.close_TCP_Conn 1:" + e1.getMessage());
		}

		try {
			if (outputStream != null)
				outputStream.close();
		} catch (Exception e2) {
			Log.e(TAG, "Exception LiveTcpGet.close_TCP_Conn 2:" + e2.getMessage());
		}

		try {
			if (streamConnection != null)
				streamConnection.close();
		} catch (Exception e3) {
			Log.e(TAG, "Exception LiveTcpGet.close_TCP_Conn 3: " + e3.getMessage());
		}

		streamConnection = null;
		outputStream = null;
		dataOutputStream = null;

	}

	public void makeRequest(byte[] bytes) {

		if (!open_TCP_Conn()) {
			finished = true;
			resultCode = ERR_GENERIC_ERROR;
			result = "";
			return;
		}

		// use a StrignBuffer to store the retrieved page contents
		StringBuffer results = new StringBuffer();

		try {
			// send the HTTP request
			dataOutputStream.write(bytes);

			if (onlySend) {
				dataOutputStream.close();
				close_TCP_Conn();
				finished = true;
				resultCode = 0; // all ok
				result = "";
			}

			dataOutputStream.close();

			// create DataInputStream on top of the socket connection
			inputStream = streamConnection.openInputStream();
			dataInputStream = new DataInputStream(inputStream);

			// retrieve the contents of the requested page from Web server
			int inputChar, total, totWaitTime;

			try {
				while ((inputChar = dataInputStream.read()) != -1) {
					// GpsTrackLive.logMsg(MIDPLogger.ERROR,"LiveTcpGet.makeRequest : Got char: "+(char)
					// inputChar);
					results.append((char) inputChar);
				}
			} catch (IOException e) {
				Log.e(TAG, "LiveTcpGet.makeRequest : Exception while reading: " + e.getMessage());
			}

			close_TCP_Conn();
			finished = true;

			Log.e(TAG, "LiveTcpGet.makeRequest  result: " + results.toString());
			// the result we get is errorcode:result text (errorcode==0 means no
			// error)
			valuePair res = valuePair.parse(results.toString(), ":");

			if (res.getIntName() == 0) {
				resultCode = 0; // all ok
				result = res.value;
			} else {
				resultCode = res.getIntName();
				result = res.value;
			}

			return;

		} catch (IOException e) {

			finished = true;
			resultCode = ERR_GENERIC_ERROR;
			result = results.toString();

			Log.e(TAG, "LiveTcpGet.makeRequest : Exception : " + e.getMessage());

			close_TCP_Conn();
			return;
		} finally {
			// free up I/O streams and close the socket connection
			close_TCP_Conn();
		}
	}

}
