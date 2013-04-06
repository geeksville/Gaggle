package com.geeksville.location;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.osmdroid.util.GeoPoint;

import android.util.Log;

public class GeoIdCorrection {
	private RandomAccessFile raf;
	ByteBuffer bb;
	private static String TAG = "GeoIdCorrection";

	public GeoIdCorrection(File geoidfile) throws FileNotFoundException {
		raf = new RandomAccessFile(geoidfile, "r");
		bb = ByteBuffer.allocate(2);
		bb.order(ByteOrder.BIG_ENDIAN);
	}

	public double getGeoidSep(double lon, double lat){
		final int lat_n = (int) ((90-lat)*2);
		final int lon_n = (int) (lon*2);
		final int offset = 2*(lat_n*360*2 + lon_n);

		final int basic_offset = 0x1A0;

		try {
			raf.seek(offset + basic_offset);

			byte b[] = new byte[2];
			raf.read(b);

			bb.clear();
			bb.put(b[0]);
			bb.put(b[1]);

			int shortVal = bb.getShort(0) & 0xFFFF;
			double d = shortVal * 0.003 -108;
			return d;
		} catch (IOException e) {
			Log.d(TAG, "Error reading GeoID sep", e);
			return 0;
		}
	}

	public double getGeoidSep(GeoPoint point){
		final double lat = point.getLatitudeE6() / 1e6;
		final double lon = point.getLongitudeE6() / 1e6;
		return getGeoidSep(lon, lat);
	}
}
