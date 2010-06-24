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

public class PrepareMsg {

	final static private int DEGREESTOINT = 46603;
	static byte[][] res_arr1 = new byte[6][];
	static byte[][] res_arr2 = new byte[4][];

	public static byte[] prepareTextMessage(String msg) {
		msg = msg + "#";

		byte[] bytes = msg.getBytes();
		byte[] headerBytes = LiveProtocol.preparePacketHeader(
		// isGPSdata,isRequest,isBinary,isSimpleHeader,noAck,packetNum
				(byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 1);

		byte[] res = new byte[bytes.length + headerBytes.length];

		for (int i = 0; i < headerBytes.length; i++) {
			res[i] = headerBytes[i];
		}

		for (int i = 0; i < bytes.length; i++) {
			res[i + headerBytes.length] = bytes[i];
		}

		return res;
	}

	public static byte[] prepareMessage(int tm, float lon, float lat, short alt, byte sog, short cog) {

		// FIXME - convert lat long
		res_arr1[0] = toByta(tm);
		res_arr1[1] = null/* lon.toBytes() */;
		res_arr1[2] = null/* lat.toBytes() */;
		res_arr1[3] = toByta(alt);
		res_arr1[4] = toByta(sog);
		res_arr1[5] = toByta((byte) (cog / 2));
		// res_arr[6]=new byte[] {'#','*'};

		byte[] res = concat(res_arr1);
		return res;
	}

	public static byte[] prepareMessage(int tm, float lon, float lat, short alt) {
		// byte[][] res_arr=new byte[4][];

		res_arr2[0] = toByta(tm);

		res_arr2[1] = new byte[3];
		res_arr2[2] = new byte[3];

		int longitude = (int) (lon * DEGREESTOINT);
		res_arr2[1][0] = (byte) (0xff & (longitude >> 16));
		res_arr2[1][1] = (byte) (0xff & (longitude >> 8));
		res_arr2[1][2] = (byte) (0xff & longitude);

		int latitude = (int) (lat * DEGREESTOINT);
		res_arr2[2][0] = (byte) (0xff & (latitude >> 16));
		res_arr2[2][1] = (byte) (0xff & (latitude >> 8));
		res_arr2[2][2] = (byte) (0xff & latitude);

		// res_arr[1]=lon.toBytes();
		// res_arr[2]=lat.toBytes();
		res_arr2[3] = toByta(alt);

		byte[] res = concat(res_arr2);
		return res;
	}

	public static int fromByta(byte[] bytes, int offset) {
		int res;
		res = 0xff000000 & (bytes[offset] << 24);
		res |= 0x00ff0000 & (bytes[offset + 1] << 16);
		res |= 0x0000ff00 & (bytes[offset + 2] << 8);
		res |= 0x000000ff & bytes[offset + 3];
		return res;
	}

	public static int fromByta(byte[] bytes) {
		return fromByta(bytes, 0);
	}

	public static short shortFromByta(byte[] bytes, int offset) {
		short res;
		res = (short) (bytes[offset] << 8);
		res &= 0xff00;
		res |= 0x00ff & bytes[offset + 1];
		return res;
	}

	public static short shortFromByta(byte[] bytes) {
		return shortFromByta(bytes, 0);
	}

	public static byte[] toByta(int data) {
		return new byte[] { (byte) ((data >> 24) & 0xff), (byte) ((data >> 16) & 0xff), (byte) ((data >> 8) & 0xff),
				(byte) ((data >> 0) & 0xff), };
	}

	public static byte[] concat(byte[][] b) {
		int totlen = 0;
		for (int i = 0; i < b.length; i++) {
			totlen += b[i].length;
		}
		byte[] res = new byte[totlen];

		for (int i = 0, k = 0; i < b.length; i++) {
			for (int j = 0; j < b[i].length; j++) {
				res[k] = b[i][j];
				k++;
			}
		}
		return res;
	}

	/*
	 * public static byte[] concat(byte header, byte[][] b) { int totlen=0;
	 * 
	 * int len=b.length;
	 * 
	 * for(int i=0;i<len;i++) { totlen+=b[i].length; } totlen++; byte[] res= new
	 * byte[totlen];
	 * 
	 * res[0]=header; for(int i=0,k=1;i<len;i++) { for(int
	 * j=0;j<b[i].length;j++) { res[k]=b[i][j]; k++; } } return res; }
	 */

	public static byte[] concat(byte[] header, byte[][] b, int len) {
		int totlen = 0;
		for (int i = 0; i < len; i++) {
			totlen += b[i].length;
		}
		totlen += header.length;
		byte[] res = new byte[totlen];

		// res[0]=header;

		for (int j = 0; j < header.length; j++) {
			res[j] = header[j];
		}

		for (int i = 0, k = header.length; i < len; i++) {
			for (int j = 0; j < b[i].length; j++) {
				res[k] = b[i][j];
				k++;
			}
		}
		return res;
	}

	/*
	 * 
	 * public static byte[] concat(byte[] A, byte[] B) { byte[] C= new
	 * byte[A.length+B.length]; System.arraycopy(A, 0, C, 0, A.length);
	 * System.arraycopy(B, 0, C, A.length, B.length); return C; }
	 */

	// public static byte[] toByta(Float data) {
	// return toByta(Float.floatToIntBits(data.floatValue()));
	// }

	public static byte[] toByta(byte data) {
		return new byte[] { data };
	}

	public static byte[] toByta(short data) {
		return new byte[] { (byte) ((data >> 8) & 0xff), (byte) ((data >> 0) & 0xff), };
	}
}
