package com.livetrack24.net;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TimeZone;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.StreamConnection;

import com.livetrack24.leoLive.GpsTrackLive;
import com.livetrack24.leoLive.MIDPLogger;
import com.livetrack24.leoLive.Preferences;
import com.livetrack24.leoLive.SMSrunner;

// import com.livetrack24.leoLive.GpsTrackLive.DatagramListener;

public class LiveNet implements Runnable {
	final static int CONN_UDP  = 0;
	final static int CONN_TCP  = 1;
	final static int CONN_HTTP = 2;
	    
	private Thread mThread=null;
    private Preferences sPreferences ;
	  
    public boolean reconnectingUDP=false;
    public boolean UDPerror=false;
    // for UDP sockets
  	public  DatagramConnection dc;
  	// public  DatagramConnection dcIn ;	
  	public  String destAddr;
  	public  Datagram dgram=null,dgramIn=null;
  	
    // for tcp streams
    public  StreamConnection streamConnection ;
    public  OutputStream outputStream ;
    public  DataOutputStream dataOutputStream ;
	// use InputStream to receive responses from Web server
    public  InputStream inputStream = null;
    public  DataInputStream dataInputStream = null;
    
    //  public  DatagramListener dgl;
    public  boolean isOver = false;
    public boolean UDP_is_open=false;
    
    private int maxNetTries;
    
    private String LoginString;
    public int sendLoginTimes;
    //public int sendLoginTimesMax;
   // public boolean sendLogin=false;
    public boolean loginHasBeenSent=false; 
    
    public boolean queueIsListening=false;
    
    // for thread waits
    private boolean quit = false;
    private Vector queue = new Vector();
    
    public boolean serverOK;
    public int packetsSend;
    public int serverPacketsReceived;
    
    private boolean packetResponseStatus;
   
 
	public LiveNet() {    		
		sPreferences = GpsTrackLive.getPreferences();
	}
	
	   public void open() {
		    if (!sPreferences.netActive) {
		    	GpsTrackLive.logMsg(MIDPLogger.INFO,"LiveNet.open: Net is not active");
		    	return;
		    }
	        close();
	        mThread = new Thread(this);
	        mThread.start();
	    }
	    
	    public void close() {
	    	if (!sPreferences.netActive) {
	    		GpsTrackLive.logMsg(MIDPLogger.INFO,"LiveNet.close: Net is not active");
	    		return;
	    	}

	        if (mThread != null) {
		    	if (! quit) {
		    	   	synchronized( queue ){
		    	   			quit=true;
			    	    	// queue.addElement( o );
			    	    	queue.notify();			    	    	
			    	    }		
			    }		    	
		    	
	            Thread thread = mThread;
	            mThread = null;
	            try {
	                thread.join();
	            }
	            catch (InterruptedException ex) {
	            	GpsTrackLive.logMsg(MIDPLogger.ERROR,"LiveNet.close: Exception: " + ex.getMessage());
	            }
	        }
	        
	        close_UDP_Conn();
	        // close_TCP_Conn();
	        
	    }


	    public void sendLogin() {
	    	if (loginHasBeenSent) return;
	    	byte[]res=PrepareMsg.prepareTextMessage(LoginString);
	    	
	    	// if we have http then send with it so all trafic goes via HTTP
	    	// in all other cases TCP is used as a more riable method to UDP
	    	if (sPreferences.gpsPointsSendMethod==2) { // HTTP WRAPPER  	    		
	    		String HexString=LiveProtocol.bin2Hex(res);
	    		String serverResponse=getHttp("http://"+sPreferences.getServerUrl()+"/track.php?prx16="+HexString);
    	    	GpsTrackLive.logMsg(MIDPLogger.INFO,"LiveNet.sendLogin : serverResponseHttp: "+ serverResponse);
    	    	if ( serverResponse!="") 
    	    		GpsTrackLive.sNet.loginHasBeenSent=true;

	    	} else {	
	    		NetSendLogin loginThread=new NetSendLogin(res);
	    	}	
	    	
	    	// now is a good time to check if SMS sending is needed
	       	if ( sendLoginTimes==0 && GpsTrackLive.isSMSApiSupported() ) {
        		if ( sPreferences.sms1_interval>0  || sPreferences.sms2_interval>0  ) {
        			boolean smsResult;	
        			String smsText="User "+sPreferences.getUserName()+" has started live tracking";
        			
        			// we need to split the login packet into header + text mesg
        			// header is 7 first bytes
        			
        			byte [] res0=new byte[7];
        			//byte [] res1=new byte[res.length-7];
        			
        			for (int i=0;i<7;i++){
        	    		res0[i]=res[i];
        	    	}        	    	
        	    	//for (int i=7;i<res.length;i++){
        	    	//	res1[i-7]=res[i];
        	    	//}
        			
        	    	// max len for this is 160 chars so we have:
        			// 4 + 7*2 +1 = 19, we have 141 left 
        			if (LoginString.length()>141) {
        				LoginString=LoginString.substring(0,140);
        			}
        				
        			String smsBinary="#hdr"+(sPreferences.getTestMode()?"1":"0")+LiveProtocol.bin2Hex(res0)+LoginString+"#";
	        		
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
	    	
	    	
	    	sendLoginTimes++;
	    }
	    
	    public void run() {
	    	if (!sPreferences.netActive) {
	    		GpsTrackLive.logMsg(MIDPLogger.WARNING,"LiveNet.run: Net is not active");
	    		return;
	    	}
	    	
	        try {        		        	
	        	serverOK=false;
	        	serverPacketsReceived=0;
	        	packetsSend=0;
	        	reconnectingUDP=false;
	        	
	        	UDPerror=false;
	        	
	            open_UDP_Conn();	

	            sendLoginTimes=0;
	            
	            queueIsListening=false;

	        	// if (sPreferences.useTCPforLogin) open_TCP_Conn();
	        	
	        	
	            // Send only one string now 
	        	LiveProtocol.init();
	            TimeZone defaultTZ = TimeZone.getDefault();      
	              
	            int current_tm=(int)(System.currentTimeMillis()/1000);

	            if (sPreferences.mSendHttpFirst) {
	            	String serverStatus=getHttp("http://"+sPreferences.serverURL+"/client.php?op=ping");
	            	Thread.sleep(5000);
	            	//serverStatus=getHttp("http://"+sPreferences.serverURL+"/client.php?op=ping");
	            	//Thread.sleep(3000);
	            }
	            
            	LoginString='U'+
            		sPreferences.getUserName()+':'+sPreferences.getUserPassword()+':'+
            		GpsTrackLive.getPlatform()+':'+sPreferences.getGPSdescription() + ':'+
            		sPreferences.getProgramName()+':'+sPreferences.getProgramVersion()+':'+
            		sPreferences.getSendEveryN() +':'+sPreferences.getMinTime()+':'+
            		current_tm+':'+defaultTZ.getID()+':'+defaultTZ.getRawOffset()+':'+
            		sPreferences.gliderType+':'+sPreferences.gliderName+':';	            	
        		
            	loginHasBeenSent=false;
	            sendLogin();
	            
	            Thread thread = Thread.currentThread();
	        	thread.sleep(300);
	        	/*
	            if (sPreferences.useTCPforLogin) {
	            	close_TCP_Conn();
	            	thread.sleep(400);
	            } else {
	            	thread.sleep(300);
	            }
	            */
	            thread=null;
	          
	            
	            // now enter an infinite loop waiting for byte[] to send 
	            byte[] inComingBytes;
	            quit=false;
	            
	        	while( !quit ){
	        		inComingBytes = null;

	        	    synchronized( queue ){
	        	    	// GpsTrackLive.logMsg(MIDPLogger.TRACE,"LiveNet.run: Entering synchronized" );
		        		if( queue.size() > 0 ){
		        			GpsTrackLive.logMsg(MIDPLogger.TRACE,"LiveNet.run: will get element from queue");
		        			inComingBytes = (byte[]) queue.elementAt( 0 );
		        		    queue.removeElementAt( 0 );
		        		} else {
		        			queueIsListening=true;
		        			GpsTrackLive.logMsg(MIDPLogger.TRACE,"LiveNet.run: will wait for queue");
		        		    try {
		        		    	queue.wait();
		        		    }
		        		    catch( Exception e ){
		        		    	// catch( InterruptedException e ){
		        		    	GpsTrackLive.logMsg(MIDPLogger.ERROR,"LiveNet.run: Exception in queue.wait() :" + e.getMessage());
		        		    }
		        		    GpsTrackLive.logMsg(MIDPLogger.DEBUG,"LiveNet.run: woke up from queue");
		        		}
	        	    }
	        	    // GpsTrackLive.logMsg(MIDPLogger.TRACE,"LiveNet.run : Out of synchronized" );
	        	    // GpsTrackLive.logMsg(MIDPLogger.INFO,"LiveNet run : Got "+inComingBytes.length+" bytes" );
	        	    if( inComingBytes != null ){
	        	    	packetsSend++;
	        	    	if (sPreferences.gpsPointsSendMethod==2) { // HTTP WRAPPER
	        	    		// set the noAck bit to 1 , we dont want ack
	        	    		inComingBytes[0] |=0x08 ;	        	    		
	        	    		String HexString=LiveProtocol.bin2Hex(inComingBytes);
	        	    		String serverResponse=getHttp("http://"+sPreferences.getServerUrl()+"/track.php?prx16="+HexString);
		        	    	GpsTrackLive.logMsg(MIDPLogger.INFO,"LiveNet.run : serverResponseHttp: "+ serverResponse);
		        	    	if ( serverResponse!="") serverOK=true;
		        	    	else serverOK=false;
	        	    	} else if (sPreferences.gpsPointsSendMethod==1) { // TCP	        	    		
	        	    		LiveTcpGet tcp=new LiveTcpGet();
	        	    		tcp.onlySend=true;
	        	    		tcp.open(sPreferences.getServerName(),inComingBytes);	        	    		
	        	    	} else if (sPreferences.gpsPointsSendMethod==3) { // TCP PROXY at 127.0.0.1	        	    		
	        	    		LiveTcpGet tcp=new LiveTcpGet();
	        	    		tcp.onlySend=true;
	        	    		tcp.open("127.0.0.1",inComingBytes);	        	    		
	        	    	} else if ( (inComingBytes[0] & 0x08) == 0 && false  ) { // the NOACK bit 
	        	    		// not used any more
	        	    		
		        	    	/*String serverResponse=sendMsgAck(inComingBytes);
		        	    	GpsTrackLive.logMsg(MIDPLogger.INFO,"LiveNet.run : serverResponse: "+ serverResponse);
		        	    	if (serverResponse!="error") serverOK=true;
		        	    	else serverOK=false;
		        	    	*/
	        	    	} else { // UDP packets
	        	    		if ( sendMsgUDP(inComingBytes) ==0 ){
	        	    			UDPerror=true;
	        	    		} else {
	        	    			UDPerror=false;
	        	    		}
	        	    	}
	        	    }
	        	}
	            
	        }
	        catch (Exception ex1) {
	        	GpsTrackLive.logMsg(MIDPLogger.ERROR,"LiveNet.run: Exception 1:" + ex1.getMessage());
	        }

	    }
	    
	    // this is the high end function for sending a packet,
	    // it only adds the packet to the main running queue
	    public boolean sendNetMsg( byte[] o ){
	    	if (!sPreferences.netActive) {
	    		GpsTrackLive.logMsg(MIDPLogger.INFO,"LiveNet.sendNetMsg: Net is not active");
	    		return false;
	    	}
	    	
	    	if (!queueIsListening) {
		    	GpsTrackLive.logMsg(MIDPLogger.WARNING,"LiveNet.sendNetMsg: queue is not yet listening");
	    		return false;
	    	}
	    	
	    	GpsTrackLive.logMsg(MIDPLogger.TRACE,"LiveNet.sendNetMsg: before synchronized");
	    	synchronized( queue ){
	    	    if( !quit ){
	    	    	GpsTrackLive.logMsg(MIDPLogger.TRACE,"LiveNet.sendNetMsg: adding to queue");
	    	    	queue.addElement( o );
	    	    	queue.notify();
	    	    	return true;
	    	    } else {
	    	    	GpsTrackLive.logMsg(MIDPLogger.TRACE,"LiveNet.sendNetMsg: quit was TRUE");
	    	    	return false;
	    	    }
	    	}
	    }
	    
	   // This is the actual function that sends UDP data over the network
	   public  int sendMsgUDP(byte[] bMsg)  {
		   if (!sPreferences.netActive) {
			   GpsTrackLive.logMsg(MIDPLogger.INFO,"LiveNet.sendMsgUDP: Net is not active");
			   return 0;
		   }
		   
		   GpsTrackLive.logMsg(MIDPLogger.DEBUG,"LiveNet.sendMsgUDP: Sending at last To Net");
		   
		   
		    if (!UDP_is_open && reconnectingUDP) {		    	
	    		GpsTrackLive.logMsg(MIDPLogger.INFO,"LiveNet.sendMsgUDP: reconnectingUDP is true and  UDP connection is not open, will try to open it");
	    		open_UDP_Conn();
		    }
		    	
		    if (!UDP_is_open) {
		    	GpsTrackLive.logMsg(MIDPLogger.INFO,"LiveNet.sendMsgUDP: UDP connection is not open");
		    	return 0;
		    }
		    
	        try {
	        	dgram.reset();
	        	dgram.write(bMsg);
	        	dc.send(dgram);
	        	return 1;
	        } catch (Exception e)  {
	        	GpsTrackLive.logMsg(MIDPLogger.ERROR,"LiveNet.sendMsgUDP: Exception: " + e.getMessage());
	        	reconnectingUDP=true;
	        	close_UDP_Conn();
	        	open_UDP_Conn();
	        	return 0;
	        }
	        
		     /*
	        finally {
	                if (dc != null) {
	                    try {
	                        dc.close();
	                    }
	                    catch (Exception e) {
	                       System.out.println("Exception Closing: " + e.getMessage());
	                    }
	                }
	            }
	            */
	    }

	   /*
	   public String sendMsgAck(byte[] bMsg){
			String serverText="error";
			for(int i=0;i<sPreferences.getMaxNetTries();i++) {
				serverText=sendMsgWithReply(bMsg);
				if (serverText!="error") break;
			}
			if (serverText=="error") {
				GpsTrackLive.logMsg(MIDPLogger.WARNING,"LiveNet.sendMsgAck: No response from server");
				serverOK=false;
				return "error";
			} else {
				serverOK=true;
				try {
					int pnum=Integer.parseInt(serverText);
					serverPacketsReceived=pnum;
				} catch (NumberFormatException en){
					
				}				
				
				return serverText;
			}
			
	   }
	    */
	    public  void open_UDP_Conn()  {  
	    	if (!sPreferences.netActive) {
	    		GpsTrackLive.logMsg(MIDPLogger.INFO,"LiveNet.open_UDP_Conn: Net is not active");
	    		return;
	    	}
	    	
	    	GpsTrackLive.logMsg(MIDPLogger.INFO,"LiveNet.open_UDP_Conn: Opening UDP Connection");
	        destAddr = "datagram://"+sPreferences.getServerName()+":"+sPreferences.getPortNum();
	        
	        dc = null;

	        try {
	          // Create a datagram socket
	           dc = (DatagramConnection)Connector.open("datagram://:"+sPreferences.getPortNum());
	           
	          // dc = (DatagramConnection)Connector.open("datagram://"+sPreferences.getServerName()+":"+sPreferences.getPortNum());
	           
	           
	           dgram= dc.newDatagram(1000,"datagram://"+sPreferences.getServerName()+":"+sPreferences.getPortNum());
	           
	           GpsTrackLive.logMsg(MIDPLogger.ERROR,"LiveNet.open_UDP_Conn: datagram://"+sPreferences.getServerName()+":"+sPreferences.getPortNum() );
	          //dc = (DatagramConnection)Connector.open(destAddr);
	          //dgram= dc.newDatagram(1000);
	          
	          // dcIn= (DatagramConnection)Connector.open("datagram://:"+sPreferences.getPortNum());
	          // dgramIn= dcIn.newDatagram(1000);
	          UDP_is_open=true;
	          reconnectingUDP=false;
	          return;
	        }
	        catch (Exception e)  {
	        	GpsTrackLive.logMsg(MIDPLogger.ERROR,"LiveNet.open_UDP_Conn: Exception: " + e.getMessage());
	        	UDP_is_open=false;
	        	// we got and exception, something bad has happened, we better close the socket
	        	close_UDP_Conn();
	        	
	        }
	        
	    }
	    
	    public  void close_UDP_Conn() {   
	    	if (!sPreferences.netActive) {
	    		GpsTrackLive.logMsg(MIDPLogger.INFO,"LiveNet.close_UDP_Conn: Net is not active");
	    		return;
	    	}
	    	
	        if ( dc != null ) {             
	            try {
	            	GpsTrackLive.logMsg(MIDPLogger.INFO,"LiveNet.close_UDP_Conn: Closing UDP connection");
	                dc.close();
	            }
	            catch (Exception e) {
	            	GpsTrackLive.logMsg(MIDPLogger.ERROR,"LiveNet.close_UDP_Conn: Exception: " + e.getMessage());
	            }            
			}

	        if (dc == null) {
	        	GpsTrackLive.logMsg(MIDPLogger.INFO,"LiveNet.close_UDP_Conn: Is already closed");
			}  	
	        UDP_is_open=false;
	    }
	    
	    
	    // --------------------------------------------------------
	    // --------------------------------------------------------
	    // Mostly old code that is not used any more
	    // --------------------------------------------------------
	    // -------------------------------------------------------- 
	    /*
	    public  void sendStringToNet(String msg)  {
	    	int connType;

	    	if (sPreferences.useHttpWrapper) connType=CONN_HTTP;
	    	else if (sPreferences.useTCPforLogin) connType=CONN_TCP ;
	    	else connType=CONN_UDP;
	    	
	    	sendStringToNet(msg,connType);	    	
	    }
	    
	    public  void sendStringToNet(String msg,int connType)  {
	    	if (!sPreferences.netActive) {
	    		GpsTrackLive.logMsg(MIDPLogger.INFO,"LiveNet.sendStringToNet: Net is not active");
	    		return;
	    	}
	    	
	    	try {
		    	if (connType==CONN_UDP || connType==CONN_HTTP) {
		    		msg=msg+"#";    	
		    	}
		    	
		    	byte[] bytes=msg.getBytes();
		    	
		    	byte noAck=0;

		    	if (connType==CONN_TCP || connType==CONN_HTTP ) noAck=1; 
		
		    	byte[] headerBytes=LiveProtocol.preparePacketHeader(
		    	       //isGPSdata,isRequest,isBinary,isSimpleHeader,noAck,packetNum    			
		    			(byte)0,(byte) 0,(byte)0, (byte) 0 ,  (byte) noAck  ,(byte) 1  );
		    	
		    	byte[] res=new byte[bytes.length+headerBytes.length];
		    	
		    	for (int i=0;i<headerBytes.length;i++){
		    		res[i]=headerBytes[i];
		    	}
		    	
		    	for (int i=0;i<bytes.length;i++){
		    		res[i+headerBytes.length]=bytes[i];
		    	}
		    	
		    	GpsTrackLive.logMsg(MIDPLogger.DEBUG,"LiveNet.sendStringToNet: Send: "+msg);
		    	packetResponseStatus=false;
		    	
		    	if (connType==CONN_UDP) {  // UDP
		    		if (sPreferences.serverResponse) {
			    		String serverText="error";
			    		for(int i=0;i<sPreferences.getMaxNetTries();i++) {
			    			serverText=sendMsgWithReply(res);
			    			if (serverText!="error") break;
			    		}
			    		if (serverText=="error") {
			    			GpsTrackLive.logMsg(MIDPLogger.ERROR,"LiveNet.sendStringToNet: No response from server");
			    			serverOK=false;		    			
			    		} else {
			    			GpsTrackLive.logMsg(MIDPLogger.INFO,"LiveNet.sendStringToNet: GOT OK from server");
			    			serverPacketsReceived++;
			    			serverOK=true;
			    		}
		    		} else {
		    			sendMsg(res);
		    			serverOK=true;
		    		}
		    	} else if (connType==CONN_TCP) {  // TCP
		    		sendMsgTCP(res);
		    		serverOK=true;
		    	} else { // HTTP WRAPPER       	    	
    	    		String HexString=LiveProtocol.bin2Hex(res);
    	    		String serverResponse=getHttp("http://"+sPreferences.getServerUrl()+"/track.php?prx16="+HexString);
        	    	GpsTrackLive.logMsg(MIDPLogger.INFO,"LiveNet.sendStringToNet : serverResponseHttp: "+ serverResponse);
        	    	if ( serverResponse!="") serverOK=true;
        	    	else serverOK=false;
		    	}

		    	packetResponseStatus=true;
	    	}   catch (Exception e)  {
	    		GpsTrackLive.logMsg(MIDPLogger.ERROR,"LiveNet.sendStringToNet: Exception: " + e.getMessage());
	    		serverOK=false;
	        }
	    }
	    	    
	
	   public String sendMsgWithReply(byte[] bMsg)  {
		    if (!sPreferences.netActive) {
		    	GpsTrackLive.logMsg(MIDPLogger.INFO,"LiveNet.sendMsgWithReply: Net is not active");
		    	return "error";
		    }
		   
		    if (!UDP_is_open) {
		    	GpsTrackLive.logMsg(MIDPLogger.INFO,"LiveNet.sendMsgWithReply: UDP connection is not open");
		    	return "";
		    }

		    
		    //LiveNetListener dgl = new LiveNetListener ("datagram://:" + sPreferences.getPortNum(),dc,this);
			DatagramListener dgl = new DatagramListener("datagram://:" + sPreferences.getPortNum());
			dgl.start();
			
			sendMsg(bMsg);					
		 
			int counter = 0;
			Thread thread = Thread.currentThread();	 
			
	  		while (!isOver) {
			  if (counter++ > 7) break;
			  GpsTrackLive.logMsg(MIDPLogger.DEBUG,"LiveNet.sendMsgWithReply: will sleep for 600 msec");
				
			  try {
            	  thread.sleep(600);
			  } catch (Exception e) {
				  GpsTrackLive.logMsg(MIDPLogger.ERROR,"LiveNet.sendMsgWithReply: Exception: "+e.getMessage());			      
			  }
			  
			}
	  		  thread=null;
	  		
	  		  isOver = false;
			  dgl.stop();
			  String result=dgl.data;
			  dgl=null;
			  
			  GpsTrackLive.logMsg(MIDPLogger.DEBUG,"LiveNet.sendMsgWithReply: Got: "+result);
			  return result;			 
		  
		   }
		   
		   class DatagramListener extends Thread {
		       
		       // DatagramConnection dc = null;
		       Datagram dobject;
		       String data = "error";

		       DatagramListener(String recvAddr) {
			       try {  
			              // dc = (DatagramConnection)Connector.open(recvAddr);
			              dobject = dc.newDatagram(dc.getMaximumLength());
			              // dobject.reset();
			              // ********************************
			              // ********************************
			              // maybe different LENGHT ? getNominalLength() ???
			              
			              
			              //dobject = dcIn.newDatagram(500);
				   } catch (Exception e) {
					   GpsTrackLive.logMsg(MIDPLogger.ERROR,"DatagramListener: Failed to initialize Connector");
				   }
		       }
		       
				public void stop(){
					dobject.reset();
					dobject=null;
						//if (dc != null) {
			            //   try {           
			            // dc.close();
			            //   } catch (Exception f) {
			            //	 GpsTrackLive.logMsg(MIDPLogger.ERROR,"stop: Failed to close Connector: "+ f.getMessage());
			            //   }
			           //  }
			             
				}
				
		       public void run() {
		    	   data = "error";
		    	   try {
		    		 dc.receive(dobject);
		           } catch (Exception e) {
				        	 GpsTrackLive.logMsg(MIDPLogger.ERROR,"DatagramListener.run: Failed to receive message: "+ e.getMessage());
				        	 isOver = true; 
		           }
		           
		           try {
		    		 // extract data from the Datagram object dobject
		    	     byte[] receiveData = dobject.getData();
		    	     int receiveLength = dobject.getLength();
		    	     GpsTrackLive.logMsg(MIDPLogger.DEBUG,"DatagramListener.run: Received datagram with length: "+ receiveLength);
		    	        
		             // data = new String(dobject.getData(), 0, dobject.getLength());
		    	     // data = new String(receiveData);
		    	     data = new String(receiveData,0,receiveLength);

		             isOver = true; 
		          } catch (Exception e) {
		        	 GpsTrackLive.logMsg(MIDPLogger.ERROR,"DatagramListener.run: Failed to receive message 2: "+ e.getMessage());
		        	 isOver = true; 
		          } finally {
		             if (dc != null) {
		               try {           
		              //   dc.close();
		               } catch (Exception f) {
				         GpsTrackLive.logMsg(MIDPLogger.ERROR,"DatagramListener.run: Failed to close Connector: "+ f.getMessage());		                 
		               }
		             }
		          }
		       }
		   }
		   
	   */
//		--------------------------------------------------------------------
//		 --------------------------------------------------------------------
//		 --------------------------------------------------------------------
		/*   
		   public  void open_TCP_Conn(String ServerName)  {
			   if (!sPreferences.netActive) {
				   GpsTrackLive.logMsg(MIDPLogger.INFO,"open_TCP_Conn : net is not active");
				   return;
			   }
			   
		     if (dataOutputStream != null && outputStream != null && streamConnection != null) {
		    	 GpsTrackLive.logMsg(MIDPLogger.INFO,"open_TCP_Conn : TCP is already open");
				 return;		    	 
		     }
				    	 
		    if (false) { 
			   	streamConnection = null;		   	 
			   	// use OutputStream to send requests
			   	outputStream = null;
			   	dataOutputStream = null;
		    }
		    
		   	// specify the connect string
		   	String connectString = "socket://"+ServerName+":"+sPreferences.getPortNum();
		   	GpsTrackLive.logMsg(MIDPLogger.INFO,"Opening TCP Connection at "+connectString);
		   try {
		       // establish a socket connection with remote server
		       streamConnection = (StreamConnection) Connector.open(connectString);
		       
		       // create DataOuputStream on top of the socket connection
		       outputStream = streamConnection.openOutputStream();
		       dataOutputStream = new DataOutputStream(outputStream);

		         
		     } catch (IOException e) {
		   	  	GpsTrackLive.logMsg(MIDPLogger.ERROR,"Exception open_TCP_Conn:" + e.getMessage());
		     }
		   }

		   public  void close_TCP_Conn()  {
			   if (!sPreferences.netActive) {
				   GpsTrackLive.logMsg(MIDPLogger.INFO,"close_TCP_Conn : net is not active");
				   return;
			   }
			   
			   GpsTrackLive.logMsg(MIDPLogger.INFO,"Closing TCP connection");
			   try {
			     if (dataOutputStream != null)
			       dataOutputStream.close();
			   } catch (Exception e1) {
				   GpsTrackLive.logMsg(MIDPLogger.ERROR,"Exception close_TCP_Conn 1:" + e1.getMessage());
			   }
			   
			   try {
			     if (outputStream != null)
			       outputStream.close();
			   } catch (Exception e2) {
				   GpsTrackLive.logMsg(MIDPLogger.ERROR,"Exception close_TCP_Conn 2:" + e2.getMessage());
			   }
			   
			   try {
			     if (streamConnection != null)
			       streamConnection.close();
			   } catch (Exception e3) {
				   GpsTrackLive.logMsg(MIDPLogger.ERROR,"Exception close_TCP_Conn 3: " + e3.getMessage());
			   }
			   
				streamConnection = null;			   	  			 
			   	outputStream = null;
			   	dataOutputStream = null;

		   }
		   
		   public  void sendMsgTCP(byte[] msg)  {
			   if (!sPreferences.netActive){
				   GpsTrackLive.logMsg(MIDPLogger.INFO,"sendMsgTCP : net is not active");
				   return;
			   }
			   
            	open_TCP_Conn(sPreferences.getServerName());
			   	try {
			   		dataOutputStream.write(msg,0,msg.length);
			   		dataOutputStream.flush();
			   		serverOK=true;
			   	} catch (IOException e) {
			   		GpsTrackLive.logMsg(MIDPLogger.ERROR,"Exception sendMsgTCP:" + e.getMessage());
			   		serverOK=false;
			   	}		   	
            	close_TCP_Conn();
		   	
		   }
		   
*/
		   public static String getHttp(String url) {
			   HttpConnection connection = null;
			      InputStream inputstream = null;
			      try
			      {
			        connection = (HttpConnection) Connector.open(url);
			        //HTTP Request
			        connection.setRequestMethod(HttpConnection.GET);
			        connection.setRequestProperty("Content-Type","//text plain");
			        connection.setRequestProperty("Connection", "close");

			        // System.out.println(url);
			        // HTTP Response
			        // System.out.println("Status Line Code: " + connection.getResponseCode());
			        // System.out.println("Status Line Message: " + connection.getResponseMessage());
			        if (connection.getResponseCode() == HttpConnection.HTTP_OK) {
			          // System.out.println(connection.getHeaderField(0)+ " " + connection.getHeaderFieldKey(0));        
			          // System.out.println("Header Field Date: " + connection.getHeaderField("date"));
			          
			          String str;
			          inputstream = connection.openInputStream();
			          int length = (int) connection.getLength();
			          if (length != -1) {
			            byte incomingData[] = new byte[length];
			            inputstream.read(incomingData);
			            str = new String(incomingData);
			          } else {
			            ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
			            int ch;
			            while ((ch = inputstream.read()) != -1) {
			              bytestream.write(ch);
			            }
			            str = new String(bytestream.toByteArray());
			            bytestream.close();
			          }
			          // System.out.println(str);
			          return str;

			        }
			      } catch(IOException error){
			    	  System.out.println("Caught IOException: " + error.toString());
			      }
			      
			      finally {
			        if (inputstream!= null) {
			          try  { 
			            inputstream.close();
			          } catch( Exception error) {
			             //log error
			          }
			        }
			        
			        if (connection != null) {
			          try {
			             connection.close();
			          } catch( Exception error){
			             //log error 
			          }
			        }
			        
			      }
			      
			      return "";
		   }
		   
		   /*
			 public String httpPost(String url,String data) {
				 
				HttpConnection httpConn = null;
			    // String url = "http://localhost:8080/examples/servlet/GetBirthday";
			    InputStream is = null;
			    OutputStream os = null;

			    try {
			      // Open an HTTP Connection object
			      httpConn = (HttpConnection)Connector.open(url);
			      // Setup HTTP Request to POST
			      httpConn.setRequestMethod(HttpConnection.POST);

			      httpConn.setRequestProperty("User-Agent",
			        "Profile/MIDP-1.0 Confirguration/CLDC-1.0");
			      httpConn.setRequestProperty("Accept_Language","en-US");
			      //Content-Type is must to pass parameters in POST Request
			      httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			      // This function retrieves the information of this connection
			      //getConnectionInformation(httpConn);


			      os = httpConn.openOutputStream();

			      //String params;
			      //params = "name=" + name;

			      os.write(data.getBytes());

			      //Caution: os.flush() is controversial. It may create unexpected behavior
			      ///      on certain mobile devices. Try it out for your mobile device 

			      //os.flush();

			      // Read Response from the Server

			      StringBuffer sb = new StringBuffer();
			      is = httpConn.openDataInputStream();
			      int chr;
			      while ((chr = is.read()) != -1)
			        sb.append((char) chr);

			      // Web Server just returns the birthday in mm/dd/yy format.
			      //System.out.println(name+"'s Birthday is " + sb.toString());
			      	return sb.toString();
			    } catch(IOException error){
			    	GpsTrackLive.logMsg(MIDPLogger.ERROR,"httpPost: IOException:" + error.getMessage());
				    //   System.out.println("Caught IOException: " + error.toString());	
			    } finally {
		           try {
				      if(is!= null) 
				    	is.close();
				      if(os != null)
				          os.close();
				      if(httpConn != null)
				           httpConn.close();
			    	} catch( Exception error) {
		             // log error
		          	}
			    }
			    
			    return "";
			 }
			 */

}
