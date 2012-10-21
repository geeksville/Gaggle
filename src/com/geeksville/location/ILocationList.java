/****************************************************************************************
 * Gaggle is Copyright 2010, 2011, and 2012 by Kevin Hester of Geeksville Industries LLC 
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

import org.andnav.osm.util.GeoPoint;

public interface ILocationList extends Iterable<GeoPoint> {

	public abstract int numPoints();

	public abstract GeoPoint getGeoPoint(int i);

	/**
	 * Altitude in millimeters
	 * 
	 * @param i
	 *            point num
	 * @return
	 */
	public int getAltitudeMM(int i);

	/**
	 * Number of milliseconds since start of tracklog
	 * 
	 * @param i
	 * @return
	 */
	public int getTimeMsec(int i);
}
