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
// J2ME GPS Track
// Copyright (C) 2006 Dana Peters
// http://www.qcontinuum.org/gpstrack

package com.livetrack24.net;

import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

public class SendEndOfTrack extends Form implements CommandListener, Runnable {

	private Timer mTimer;
	private Thread mThread = null;

	private int timesCalled;
	private int socketTimeoutSecs;

	private LiveTcpGet tcpNet;

	String lastPosition;


	public void run() {
		/*
		 * String endString="E:"+sPreferences.problemID+":L:0#";
		 * 
		 * byte[] bytes=endString.getBytes(); byte[]
		 * headerBytes=LiveProtocol.preparePacketHeader(
		 * //isGPSdata,isRequest,isBinary,isSimpleHeader,noAck,packetNum
		 * (byte)0,(byte) 0,(byte)0, (byte) 0 , (byte) 1 ,(byte) 1 );
		 * 
		 * byte[] res=new byte[bytes.length+headerBytes.length];
		 * 
		 * for (int i=0;i<headerBytes.length;i++){ res[i]=headerBytes[i]; }
		 * 
		 * for (int i=0;i<bytes.length;i++){ res[i+headerBytes.length]=bytes[i];
		 * }
		 */

		byte[] res = PrepareMsg.prepareTextMessage("E:" + sPreferences.problemID + ":L:0:" + lastPosition);

		tcpNet = new LiveTcpGet();
		tcpNet.open(sPreferences.getServerName(), res);

		timesCalled = 0;
		mTimer = new Timer();
		mTimer.schedule(new EndProgressTimerTask(), 100, 1000);
	}

	public class EndProgressTimerTask extends TimerTask {
		public void run() {
			timesCalled++;
			if (tcpNet.isFinished() || timesCalled > socketTimeoutSecs) {

				mTimer.cancel();

				if (tcpNet.isFinished() && tcpNet.getResultCode() == 0) {
					// great, we reset the lastNotTerminatedSessionID
					sPreferences.lastNotTerminatedSessionID = 0;
					sPreferences.saveRunning();

					// we got some info back, display it inthe correct way!

					// OK:0:packetsTotal:packetsLost:pointsNum:duration in
					// secs:max distance in km:distance from takeoff in
					// km:bearting from takeoff
					// example: OK:0:853:34:3402:7310
					String Message = "";
					String[] param = StringTokenizer.getArray(tcpNet.getResult(), ":");
					if (param.length >= 9) {
						if (param[1].equalsIgnoreCase("0")) {

							int duration, totPackets, lossPackets;
							try {
								duration = Integer.parseInt(param[5]);
							} catch (Exception e) {
								duration = 0;
							}
							try {
								totPackets = Integer.parseInt(param[2]);
								if (totPackets == 0)
									totPackets = 1;
							} catch (Exception e) {
								totPackets = 1;
							}
							try {
								lossPackets = Integer.parseInt(param[3]);
							} catch (Exception e) {
								lossPackets = 0;
							}

							// Message+=param[1]+"#"+param[2]+"#"+param[3]+"#"+param[4]+"#"+param[5]+"#"+param[6]+"#";
							// Message+=param[7]+"#"+param[8]+"#";

							Message += "Info from the server:\n";
							Message += "Track Points: " + param[4] + "\n";
							Message += "Packet Loss: " + ((lossPackets * 100) / totPackets) + "% (" + lossPackets
									+ " of " + totPackets + ")\n";
							Message += "Duration: " + Utils.sec2time(duration) + "\n";
							Message += "Max Distance: " + param[6] + " km\n";
							Message += "TO Distance: " + param[7] + " km\n";
							Message += "Bearing from TO: " + param[8] + "°\n";
						} else {
							Message = "MESSAGE SEND: " + tcpNet.getResult();
						}
					} else {
						Message = "MESSAGE SEND: " + tcpNet.getResult();
					}

					GpsTrackLive.displayMessage("MESSAGE SEND OK", Message, nextScreen);

					// GpsTrackLive.display(Message,0,nextScreen);
				} else {
					String Message = "";
					// GpsTrackLive.display("PROBLEM, MESSAGE NOT SEND",0,nextScreen);
					if (tcpNet.isFinished()) {
						Message = "Error code: " + tcpNet.getResultCode();
					}
					GpsTrackLive.displayMessage("PROBLEM", "Message NOT SEND\n" + Message, nextScreen);
				}
			}

		}
	}

}
