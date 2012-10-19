// J2ME GPS Track
// Copyright (C) 2006 Dana Peters
// http://www.qcontinuum.org/gpstrack

package com.livetrack24.UI;

import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.StringItem;

import com.livetrack24.leoLive.GpsTrackLive;
import com.livetrack24.leoLive.MIDPLogger;
import com.livetrack24.leoLive.Preferences;
import com.livetrack24.net.LiveProtocol;
import com.livetrack24.net.LiveTcpGet;
import com.livetrack24.net.PrepareMsg;
import com.livetrack24.util.StringTokenizer;
import com.livetrack24.util.Utils;

public class SendEndOfTrack extends Form implements CommandListener,Runnable {

    private Displayable mParent,nextScreen;
    private Command  mCancelCommand;
    
    private Timer mTimer;
    private Thread mThread=null;	
    
    private int timesCalled;
    private int socketTimeoutSecs;
    
	private LiveTcpGet tcpNet;
	
    private Preferences sPreferences ;
    
    String lastPosition;
    
    public SendEndOfTrack(Displayable parent) {
        super("Sending END");
        mParent = parent;
        
		sPreferences=GpsTrackLive.getPreferences();
        
		socketTimeoutSecs=sPreferences.socketTimeoutSecs;
		
        if ( GpsTrackLive.getPreferences().isLeonardoDataValid() ) {
        	nextScreen= new AskAutoSubmit(this) ;
        } else {    
        	nextScreen= GpsTrackLive.getMainMenu();
        }
        
        append(new StringItem(null, "Sending END signal...\n"));
        
        if (GpsTrackLive.midp2Capable())
            append(new Gauge(null, false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING));  
                
        addCommand(mCancelCommand = new Command("Cancel", Command.CANCEL, 0));
        setCommandListener(this);
        
        // get last known coordinates in text
        if (GpsTrackLive.sTrack.lastEarthPosition !=null) {
        	lastPosition= GpsTrackLive.sTrack.lastEarthPosition.toSimpleString();
    	} else {
    		lastPosition="-";
    	}
    	
        GpsTrackLive.sTrack.close();
        GpsTrackLive.sGps.close();  
        GpsTrackLive.sNet.close();
  
        GpsTrackLive.logMsg(MIDPLogger.INFO,"Sending end of track signal");
		mThread = new Thread(this);
	    mThread.start();       
	
    }

	public void run () {	
        /*
        String endString="E:"+sPreferences.problemID+":L:0#";
        
        byte[] bytes=endString.getBytes();
    	byte[] headerBytes=LiveProtocol.preparePacketHeader(
    	       //isGPSdata,isRequest,isBinary,isSimpleHeader,noAck,packetNum    			
    			(byte)0,(byte) 0,(byte)0, (byte) 0 ,  (byte) 1  ,(byte) 1  );
    	
    	byte[] res=new byte[bytes.length+headerBytes.length];
    	
    	for (int i=0;i<headerBytes.length;i++){
    		res[i]=headerBytes[i];
    	}
    	
    	for (int i=0;i<bytes.length;i++){
    		res[i+headerBytes.length]=bytes[i];
    	}
    	*/
		
        byte[]res=PrepareMsg.prepareTextMessage("E:"+sPreferences.problemID+":L:0:"+lastPosition);
        

    	// now is a good time to check if SMS sending is needed
       	if ( GpsTrackLive.isSMSApiSupported() ) {
    		if ( sPreferences.sms1_interval>0  || sPreferences.sms2_interval>0  ) {
    			boolean smsResult;	
    			String smsText="User "+sPreferences.getUserName()+
    			" has ended live tracking. Report: "+
    			(sPreferences.problemID>0?AskProblem.mStatus[sPreferences.problemID]:"Everything OK")+
    			" "+lastPosition;
    			
    			String smsBinary="#hex"+(sPreferences.getTestMode()?"1":"0")+LiveProtocol.bin2Hex(res);
        		
	      		if (sPreferences.sms1_interval>0 ) {
	      			if( sPreferences.sms1_format==0)  		      		
	      				smsResult=GpsTrackLive.sendSMS(sPreferences.sms1_phone,smsText);
	      			else if( sPreferences.sms1_format==1) 
	        			smsResult=GpsTrackLive.sendSMS(sPreferences.sms1_phone,smsBinary);	
	        	}
	        	
	        	if (sPreferences.sms2_interval>0 ) {
	      			if( sPreferences.sms2_format==0)  		      		
	      				smsResult=GpsTrackLive.sendSMS(sPreferences.sms2_phone,smsText);
	      			else if( sPreferences.sms2_format==1) 
	        			smsResult=GpsTrackLive.sendSMS(sPreferences.sms2_phone,smsBinary);	
	        	}
			}
    	}
       	
        tcpNet =new LiveTcpGet();
        tcpNet.open(sPreferences.getServerName(),res);  
        
        timesCalled=0;
        mTimer = new Timer();
        mTimer.schedule(new EndProgressTimerTask(), 100, 1000);
	}
	
    public class EndProgressTimerTask extends TimerTask {
        public void run() {
        	timesCalled++;
        	if (tcpNet.isFinished() || timesCalled>socketTimeoutSecs) {
        	
                mTimer.cancel();
                                                
                if (tcpNet.isFinished() && tcpNet.getResultCode()==0 ) {
                	// great, we reset the lastNotTerminatedSessionID
                	sPreferences.lastNotTerminatedSessionID=0;
                	sPreferences.saveRunning();
                	
                	// we got some info back, display it inthe correct way!
                	
                	// OK:0:packetsTotal:packetsLost:pointsNum:duration in secs:max distance in km:distance from takeoff in km:bearting from takeoff
        			// example: OK:0:853:34:3402:7310
                	String Message="";
                	String [] param = StringTokenizer.getArray(tcpNet.getResult(),":" );
            		if (param.length>=9  )	{
            			if (param[1].equalsIgnoreCase("0") ) { 
            				
            				int duration,totPackets,lossPackets;
            				try {
            					duration=Integer.parseInt(param[5]);
            				} catch (Exception e) {
            					duration=0;
            				}            			            			            				
            				try {
            					totPackets=Integer.parseInt(param[2]);
            					if (totPackets==0) totPackets=1; 
            				} catch (Exception e) {
            					totPackets=1;
            				}
            				try {
            					lossPackets=Integer.parseInt(param[3]);            					
            				} catch (Exception e) {
            					lossPackets=0;
            				}
            				
            				//Message+=param[1]+"#"+param[2]+"#"+param[3]+"#"+param[4]+"#"+param[5]+"#"+param[6]+"#";
            				//Message+=param[7]+"#"+param[8]+"#";
            				
	            			Message+="Info from the server:\n";
	            			Message+="Track Points: "+param[4]+"\n";
	            			Message+="Packet Loss: "+((lossPackets*100)/totPackets)+"% ("+lossPackets+" of "+totPackets+")\n";
	            			Message+="Duration: "+Utils.sec2time(duration )+"\n";
	            			Message+="Max Distance: "+param[6]+" km\n";
	            			Message+="TO Distance: "+param[7]+" km\n";
	            			Message+="Bearing from TO: "+param[8]+"°\n";
            			} else {
            				Message="MESSAGE SEND: "+tcpNet.getResult();
            			}
            		} else {
            			Message="MESSAGE SEND: "+tcpNet.getResult();
            		}
            	
            		GpsTrackLive.displayMessage("MESSAGE SEND OK",Message,nextScreen);            		
            		
                	// GpsTrackLive.display(Message,0,nextScreen);
                } else {
                	String Message="";
                	// GpsTrackLive.display("PROBLEM, MESSAGE NOT SEND",0,nextScreen);
                	if ( tcpNet.isFinished() ) {
                		Message="Error code: "+tcpNet.getResultCode();	                		
                	}
                	GpsTrackLive.displayMessage("PROBLEM","Message NOT SEND\n"+Message,nextScreen);
                }                
            }
            	
            
        }
    }
    
    public void commandAction(Command c, Displayable d)  {
        if (c == mCancelCommand) {
            mTimer.cancel();
            // GpsTrackLive.sNet.close();
            GpsTrackLive.display("MESSAGE CANCELED, NOT SEND",3,nextScreen);
        }
    }
    
}
