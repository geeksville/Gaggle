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

import java.util.Iterator;
import java.util.Observable;

import org.andnav.osm.util.GeoPoint;

import android.os.Bundle;

import com.geeksville.util.IntArray;

/**
 * Provides a compact in memory list of a series of locations
 * 
 * @author kevinh Also supports serializing/deserializing to Android bundles for
 *         IPC.
 */
public class LocationList extends Observable implements ILocationList {

	/**
	 * latitude in _microdegrees_ (to save space/work with google maps) (degrees
	 * * 1e6)
	 */
	public IntArray latitudeE6;

	/**
	 * latitude in _microdegrees_ (to save space/work with google maps) (degrees
	 * * 1e6)
	 */
	public IntArray longitudeE6;

	/**
	 * Altitude in millimeters
	 */
	public IntArray altitudeMM;

	public IntArray timeMsec;

	/**
	 * Create an empty location list
	 */
	public LocationList() {
		latitudeE6 = new IntArray();
		longitudeE6 = new IntArray();
		altitudeMM = new IntArray();
		timeMsec = new IntArray();
	}

	/**
	 * Create a location list with reserved capacity
	 * 
	 * @param numPoints
	 */
	public LocationList(int numPoints) {
		latitudeE6 = new IntArray(numPoints);
		longitudeE6 = new IntArray(numPoints);
		altitudeMM = new IntArray(numPoints);
		timeMsec = new IntArray(numPoints);
	}

	/**
	 * Deserialize from a bundle
	 * 
	 * @param src
	 */
	public LocationList(Bundle src) {
		latitudeE6 = new IntArray(src.getIntArray("latitudes"));
		longitudeE6 = new IntArray(src.getIntArray("longitudes"));
		altitudeMM = new IntArray(src.getIntArray("altitudes"));
		timeMsec = new IntArray(src.getIntArray("timemsec"));
	}

	/**
	 * Deserialize from a DB
	 * 
	 * @param cursor
	 *            we will close this cursor at the end of extraction
	 */
	/*
	 * public LocationList(Cursor cursor) { int latCol =
	 * cursor.getColumnIndexOrThrow(LocationLogDbAdapter.KEY_LATITUDE); int
	 * longCol =
	 * cursor.getColumnIndexOrThrow(LocationLogDbAdapter.KEY_LONGITUDE); int
	 * altCol = cursor.getColumnIndexOrThrow(LocationLogDbAdapter.KEY_ALTITUDE);
	 * 
	 * int numPts = cursor.getCount();
	 * 
	 * latitudeE6 = new IntArray(numPts); longitudeE6 = new IntArray(numPts);
	 * altitude = new IntArray(numPts);
	 * 
	 * for (int i = 0; i < numPts; i++) { latitudeE6.add((int)
	 * (cursor.getDouble(latCol) * 1e6)); longitudeE6.add((int)
	 * (cursor.getDouble(longCol) * 1e6)); altitude.add(cursor.getInt(altCol));
	 * 
	 * cursor.moveToNext(); }
	 * 
	 * cursor.close(); }
	 */

	/**
	 * Add a point to the end of our tracklog
	 * 
	 * @param latitude
	 * @param longitude
	 * @param altitudeMM
	 */
	public void add(double latitude, double longitude, int altitudeMM, int timeMsec) {
		latitudeE6.add((int) (latitude * 1e6));
		longitudeE6.add((int) (longitude * 1e6));
		this.altitudeMM.add(altitudeMM);
		this.timeMsec.add(timeMsec);

		setChanged();
		notifyObservers();
	}

	/**
	 * Serialize to a bundle
	 * 
	 * @param bundle
	 */
	public void writeTo(Bundle bundle) {
		bundle.putIntArray("latitudes", latitudeE6.toArray());
		bundle.putIntArray("longitudes", longitudeE6.toArray());
		bundle.putIntArray("altitudes", altitudeMM.toArray());
		bundle.putIntArray("timemsec", timeMsec.toArray());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.geeksville.location.ILocationList#numPoints()
	 */
	public int numPoints() {
		return timeMsec.length(); // Read the last incremented field, so we will
									// always underestimate the number of points
									// if we have a race condition
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.geeksville.location.ILocationList#getGeoPoint(int)
	 */
	public GeoPoint getGeoPoint(int i) {
		return new GeoPoint(latitudeE6.get(i), longitudeE6.get(i));
	}

	/**
	 * Altitude in millimeters
	 * 
	 * @param i
	 *            point num
	 * @return
	 */
	public int getAltitudeMM(int i) {
		return altitudeMM.get(i);
	}

	/**
	 * Number of milliseconds since start of tracklog
	 * 
	 * @param i
	 * @return
	 */
	public int getTimeMsec(int i) {
		return timeMsec.get(i);
	}

	/**
	 * Find the # of udegs between the two furthest points (useful for zooming)
	 * 
	 * @return
	 */
	public int latitudeSpanE6() {
		return spanE6(latitudeE6);
	}

	/**
	 * Find the # of udegs between the two furthest points (useful for zooming)
	 * 
	 * @return
	 */
	public int longitudeSpanE6() {
		return spanE6(longitudeE6);
	}

	/**
	 * Find the # of udegs between the two furthest points (useful for zooming)
	 * 
	 * @return
	 */
	private int spanE6(IntArray pts) {
		int n = pts.length();

		if (n == 0)
			return 0;

		int lowest = pts.get(0);
		int highest = lowest;

		for (int i = 1; i < n; i++) {
			int p = pts.get(i);

			if (p < lowest)
				lowest = p;
			else if (p > highest)
				highest = p;
		}

		return highest - lowest;
	}

	@Override
	public Iterator<GeoPoint> iterator() {
		// TODO Auto-generated method stub
		return new Iterator<GeoPoint>() {
			private int pos = 0;

			@Override
			public boolean hasNext() {
				return pos < numPoints();
			}

			@Override
			public GeoPoint next() {
				return getGeoPoint(pos++);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

}
