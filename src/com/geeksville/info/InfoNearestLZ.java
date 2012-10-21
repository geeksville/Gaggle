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
package com.geeksville.info;

import com.geeksville.gaggle.GaggleApplication;
import com.geeksville.gaggle.R;
import com.geeksville.location.ExtendedWaypoint;

public class InfoNearestLZ extends InfoDistWaypoint {

	@Override
	public String getLabel() {
		return context.getString(R.string.distance_to_lz);
	}

	/**
	 * 
	 * @see com.geeksville.info.InfoField#getLabel()
	 */
	@Override
	public String getShortLabel() {
		return context.getString(R.string.distance_to_lz_short);
	}

	@Override
	protected ExtendedWaypoint getWaypoint() {
		if (context != null) {
			GaggleApplication app = (GaggleApplication) context.getApplication();
			ExtendedWaypoint w = app.getWaypoints().getNearestLZ();

			return w;
		} else
			return null;
	}
}
