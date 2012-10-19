// J2ME GPS Track
// Copyright (C) 2006 Dana Peters
// http://www.qcontinuum.org/gpstrack

package com.livetrack24.net;

import java.util.Timer;
import java.util.TimerTask;

import com.livetrack24.leoLive.GpsTrackLive;
import com.livetrack24.leoLive.MIDPLogger;
import com.livetrack24.leoLive.Preferences;

public class NetSendLogin implements Runnable {
	
    
    private Timer mTimer;
    private int timesCalled;
    private int socketTimeoutSecs;
    
    private Preferences sPreferences ;
    
    private LiveTcpGet tcpNet;
    private Thread mThread=null;	
    private byte[] msg;
    
    // the first packet of the live track with track / pilot info 
    public NetSendLogin(byte[] byteMsg) {
    	msg=byteMsg;
 		sPreferences=GpsTrackLive.getPreferences(); 		
 		socketTimeoutSecs=sPreferences.socketTimeoutSecs; 		 		
        
        GpsTrackLive.logMsg(MIDPLogger.INFO,"NetSendLogin: Send Login packet to Server");

 		//run();
		mThread = new Thread(this);
	    mThread.start();
    }

	public void run () {           
    	
        tcpNet =new LiveTcpGet();
                
        tcpNet.open(sPreferences.getServerName(),msg);  
        
        timesCalled=0;
        mTimer = new Timer();       
        mTimer.schedule(new NetSendLoginTimerTask(), 100, 1000);   
	}
	
    public class NetSendLoginTimerTask extends TimerTask {
    	public NetSendLoginTimerTask() {
    	
    	}
    	
        public void run() {
        	timesCalled++;
        	if (tcpNet.isFinished() || timesCalled>socketTimeoutSecs) {         	
                mTimer.cancel();
                if (tcpNet.isFinished() && tcpNet.getResultCode()==0) {
                	GpsTrackLive.logMsg(MIDPLogger.INFO,"NetSendLogin: Login packet send OK");
                	GpsTrackLive.sNet.loginHasBeenSent=true;
                } else {
                	GpsTrackLive.logMsg(MIDPLogger.ERROR,"NetSendLogin: Login packet NOT send");                	
                }
            }            
        }
        
    }
    
    
}