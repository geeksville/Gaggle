/* ***************************************************************************
 * MIDPLogger.java
 *
 *
 ****************************************************************************/
package com.livetrack24.net;

import java.util.Random;

import com.livetrack24.leoLive.GpsTrackLive;
import com.livetrack24.leoLive.Preferences;


public class LiveProtocol {
	
	
	public static int  sessionID;
	public static short packetID;


	public LiveProtocol(int  initSessionID) {
	       init(initSessionID);            
	}

	public LiveProtocol() {
       init();            
    }
      
	 public static void init() {
		 Preferences preferences = GpsTrackLive.getPreferences();
		   int userID=preferences.mUserID;

		   Random a= new Random( System.currentTimeMillis() );
	       int rnd=Math.abs( a.nextInt() );		   		   		   
		   
		   if (userID>0) { 
			   // we make an int with leftmost bit=1 , 
			   // the next 7 bits random (so that the same userID can have multiple 
			   // active sessions
			   // and the next 3 bytes the userID 
			   rnd=( rnd &  0xFF000000 ) | ( userID & 0x00ffffff) | 0x80000000;		   
		   } else { // we leave rnd as is BUT set leftmost bit=0
			   rnd&=0x7fffffff;
		   } 
		   init(rnd);
	 }
	 
   public static void init(int rnd) {
	   LiveProtocol.sessionID=rnd;	   
       LiveProtocol.packetID=1;       
   }
   
   public static void  decrPacketID() {
	   LiveProtocol.packetID--;	   
   }
   
   /*
   1	isGPSdata  		1=gps 		0=info 
	2   isRequest  		1=request 	0=signal
	3	isBinary 		1=binary  	0=text
	4	isSimpleHeader 	1=simple header 0=  extended   
	5   noAck			1= no ack required 0=> send ack +status
	
	IF isGPSdata && isBinary
		6-8	packetNum 0-7 gps packets num ( +1) so is 1-8 packets num 
	ELSE
	    6-8	type of text 
	    1->Login
	    2->Info
	    
	    4->full TEXT gps packet with username:pass:mobilePhoneNum also
	    Used for sessionless protocols/clients
	    format programName:programVersion:username:pass:mobilePhoneNum:time:lat:lon:alt:sog:cog:extraStr
	    
	    7->
	      
	    678
	    
	*/
   public static byte[] preparePacketHeader(
   		byte isGPSdata,byte isRequest,
   		byte isBinary,byte isSimpleHeader,byte noAck,byte packetNum
   		)  
   {
   	
	   	byte[] header;
	   	
	   	if ( isSimpleHeader == 1 ) {
	   		header=new byte[1];
	   	} else {
	   		header=new byte[7]; 
	   		byte[] tmp1=PrepareMsg.toByta(LiveProtocol.sessionID);
	   		byte[] tmp2=PrepareMsg.toByta(LiveProtocol.packetID);
	   		LiveProtocol.packetID++;
	   		
	   		header[1]=tmp1[0];
	   		header[2]=tmp1[1];
	   		header[3]=tmp1[2];
	   		header[4]=tmp1[3];
	   		
	   		header[5]=tmp2[0];
	   		header[6]=tmp2[1];
		}	
	   	    	
	   	header[0]=(byte) ( isGPSdata<<7 | isRequest<<6 | isBinary <<5 | 
	   				isSimpleHeader<<4 | noAck <<3 | ( packetNum & 0x07 )  ); 
	
	   	return header;
   	
   }

   public static String bin2Hex(byte[] block) {
	    StringBuffer buf = new StringBuffer();
	    char[] hexChars = { 
	        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
	        'A', 'B', 'C', 'D', 'E', 'F' };
	    int len = block.length;
	    int high = 0;
	    int low = 0;
	    for (int i = 0; i < len; i++) {
	        high = ((block[i] & 0xf0) >> 4);
	        low = (block[i] & 0x0f);
	        buf.append(hexChars[high]);
	        buf.append(hexChars[low]);
	    } 
	    return buf.toString();
	}
	
      
}
