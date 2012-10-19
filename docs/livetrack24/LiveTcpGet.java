package com.livetrack24.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import com.livetrack24.leoLive.GpsTrackLive;
import com.livetrack24.leoLive.MIDPLogger;
import com.livetrack24.leoLive.Preferences;
import com.livetrack24.util.valuePair;

public class LiveTcpGet implements Runnable {
	final static int ERR_NETWORK_IS_DISABLED = 1;
	final static int ERR_NETWORK_ERROR= 2;
	final static int ERR_SERVER_TIMEOUT = 3;
	final static int ERR_BAD_REQUEST = 4;
	final static int ERR_PROXY_URL_NOT_RESPONDING=10;
	final static int ERR_GENERIC_ERROR= 99;
	
	private Thread mThread = null;
	private Preferences sPreferences;

	// for tcp streams
	public StreamConnection streamConnection=null;
	public OutputStream outputStream=null;
	public DataOutputStream dataOutputStream=null;
	
	// use InputStream to receive responses from Web server
	public InputStream inputStream = null;
	public DataInputStream dataInputStream = null;

	private String serverToUse;	
	private byte[] requestBytes;
		
	private boolean finished=false;
	private int resultCode=-1;
	private String result="";
		
	public boolean onlySend=false;
	
	public LiveTcpGet() {
		sPreferences = GpsTrackLive.getPreferences();
		mThread = null;
	}

	public void open(String server,byte[] pRequestBytes) {
		if (!sPreferences.netActive) {
			GpsTrackLive.logMsg(MIDPLogger.INFO,"LiveTcpGet.open: Net is not active");
			
			finished=true;
			resultCode=ERR_NETWORK_IS_DISABLED;
			result="";
			return;
		}
		
		close();
		
		requestBytes=pRequestBytes;
		serverToUse=server;
		mThread = new Thread(this);
		mThread.start();
	}
	
	public void open(String server,String pRequestString) {
		open(server,pRequestString.getBytes());
	}

	public void close() {
		if (!sPreferences.netActive) {
			GpsTrackLive.logMsg(MIDPLogger.INFO,
					"LiveNet.close: Net is not active");
			return;
		}

		if (mThread != null) {
			Thread thread = mThread;
			mThread = null;
			try {
				thread.join();
			} catch (InterruptedException ex) {
				GpsTrackLive.logMsg(MIDPLogger.ERROR,
						"LiveTcpGet.close: Exception: " + ex.getMessage());
			}
		}

		close_TCP_Conn();

	}

	public boolean isFinished() {
		return finished ;
	}

	public int getResultCode() {
		return resultCode;
	}
	public String getResult() {
		return result ;
	}
    
	public void run() {
		GpsTrackLive.logMsg(MIDPLogger.INFO,"LiveTcpGet.run()  :enter");
		if (!sPreferences.netActive) {
			GpsTrackLive.logMsg(MIDPLogger.WARNING,"LiveTcpGet.run: Net is not active");
			finished=true;
			resultCode=ERR_NETWORK_IS_DISABLED;
			result="";
			return;
		}
		try {			
			makeRequest(requestBytes);
			return ;
		} catch (Exception ex1) {
			GpsTrackLive.logMsg(MIDPLogger.ERROR, "LiveTcpGet.run: Exception 1:"
					+ ex1.getMessage());
		}
		
	}

	// --------------------------------------------------------------------
	// --------------------------------------------------------------------
	// --------------------------------------------------------------------

	public boolean open_TCP_Conn() {
		if (!sPreferences.netActive) {
			GpsTrackLive.logMsg(MIDPLogger.INFO,"LiveTcpGet.open_TCP_Conn : net is not active");
			finished=true;
			resultCode=ERR_NETWORK_IS_DISABLED;
			result="";
			return false;
		}

		if (dataOutputStream != null || outputStream != null || streamConnection != null) {
			GpsTrackLive.logMsg(MIDPLogger.WARNING,"LiveTcpGet.open_TCP_Conn : TCP is already open");
			close_TCP_Conn() ;
			/*
			 finished=true;
			resultCode=ERR_GENERIC_ERROR;
			result="";
			return false;
			*/
		}

		if (false) {
			streamConnection = null;
			// use OutputStream to send requests
			outputStream = null;
			dataOutputStream = null;
		}

		// specify the connect string
		String connectString = "socket://" + serverToUse + ":"+ sPreferences.getPortNum();
		GpsTrackLive.logMsg(MIDPLogger.INFO, "LiveTcpGet: Opening TCP Connection at "
				+ connectString);
		
		try {
			// establish a socket connection with remote server
			streamConnection = (StreamConnection) Connector.open(connectString);
		} catch (IOException e) {
			GpsTrackLive.logMsg(MIDPLogger.ERROR, "Exception # 1 LiveTcpGet.open_TCP_Conn:"
					+ e.getMessage());
			finished=true;
			resultCode=ERR_GENERIC_ERROR;
			result="";
			return false;
		}

		try {
			// create DataOuputStream on top of the socket connection
			outputStream = streamConnection.openOutputStream();
			dataOutputStream = new DataOutputStream(outputStream);

		} catch (IOException e) {
			GpsTrackLive.logMsg(MIDPLogger.ERROR, "Exception # 2 LiveTcpGet.open_TCP_Conn:"
					+ e.getMessage());
			finished=true;
			resultCode=ERR_GENERIC_ERROR;
			result="";
			return false;
		}
		
		return true;
	}

	public void close_TCP_Conn() {
		if (!sPreferences.netActive) {
			GpsTrackLive.logMsg(MIDPLogger.INFO,"LiveTcpGet.close_TCP_Conn : net is not active");
			finished=true;
			resultCode=ERR_NETWORK_IS_DISABLED;
			result="";
			return;
		}

		GpsTrackLive.logMsg(MIDPLogger.INFO, "LiveTcpGet Closing TCP connection");
		try {
			if (dataOutputStream != null)
				dataOutputStream.close();
		} catch (Exception e1) {
			GpsTrackLive.logMsg(MIDPLogger.ERROR, "Exception LiveTcpGet.close_TCP_Conn 1:"
					+ e1.getMessage());
		}

		try {
			if (outputStream != null)
				outputStream.close();
		} catch (Exception e2) {
			GpsTrackLive.logMsg(MIDPLogger.ERROR, "Exception LiveTcpGet.close_TCP_Conn 2:"
					+ e2.getMessage());
		}

		try {
			if (streamConnection != null)
				streamConnection.close();
		} catch (Exception e3) {
			GpsTrackLive.logMsg(MIDPLogger.ERROR,
					"Exception LiveTcpGet.close_TCP_Conn 3: " + e3.getMessage());
		}

		streamConnection = null;
		outputStream = null;
		dataOutputStream = null;

	}

	public void makeRequest(byte[] bytes) {
		if (!sPreferences.netActive) {
			GpsTrackLive.logMsg(MIDPLogger.INFO,"LiveTcpGet.makeRequest : net is not active");
			finished=true;
			resultCode=ERR_NETWORK_IS_DISABLED;
			result="";
			return;
		}

		if ( ! open_TCP_Conn() ) {
			finished=true;
			resultCode=ERR_GENERIC_ERROR;
			result="";
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
				finished=true;				
				resultCode=0; //all ok
				result="";				
			}
			
			// this may cause problems
			if (sPreferences.flushTCP) { // sony (and windows Mob) does not like closing the output stream
				dataOutputStream.flush();
			} else {	
				dataOutputStream.close();
			}
			
			// create DataInputStream on top of the socket connection
			inputStream = streamConnection.openInputStream();
			dataInputStream = new DataInputStream(inputStream);
			
			// retrieve the contents of the requested page from Web server
			int inputChar,total,totWaitTime;
			

			try {
				while ((inputChar = dataInputStream.read()) != -1) {					
					// GpsTrackLive.logMsg(MIDPLogger.ERROR,"LiveTcpGet.makeRequest : Got char: "+(char) inputChar);
					results.append((char) inputChar);
				}
			} catch (IOException e) {
				GpsTrackLive.logMsg(MIDPLogger.ERROR,"LiveTcpGet.makeRequest : Exception while reading: "+e.getMessage());				
			}		
			
			close_TCP_Conn();			
			finished=true;
			
			GpsTrackLive.logMsg(MIDPLogger.INFO,"LiveTcpGet.makeRequest  result: "+results.toString() );
			// the result we get is errorcode:result text (errorcode==0 means no error)
			valuePair res=valuePair.parse(results.toString(),":");
			
			if ( res.getIntName() ==0 ) {
				resultCode=0; //all ok
				result=res.value;
			} else {
				resultCode=res.getIntName(); 
				result=res.value;
			}
					
			return;

		} catch (IOException e) {
			
			finished=true;
			resultCode=ERR_GENERIC_ERROR; 
			result=results.toString();
			
			GpsTrackLive.logMsg(MIDPLogger.ERROR,"LiveTcpGet.makeRequest : Exception : "+e.getMessage());
			
			close_TCP_Conn();
			return;
		} finally {
			// free up I/O streams and close the socket connection
			close_TCP_Conn();
		}
	}

}
