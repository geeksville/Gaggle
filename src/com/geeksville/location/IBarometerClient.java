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

import java.util.Observer;

import android.location.Location;

public interface IBarometerClient {

  /** Cheezy trick to apply preferences changes immediately on view change */
  public abstract void addObserver(Observer observer);

  public abstract void deleteObserver(Observer observer);

  /**
   * Given a GPS based altitude, reverse engineer what the correct reference
   * pressure is 
   */
  public abstract void setAltitude(float meters);

  /**
   * Return altitude in meters
   * 
   * @return altitude in meters
   */
  public abstract float getAltitude();

  /**
   * Return pressure in hectoPascal
   * 
   * @return pressure in hectopascal (100Pa)
   */
  public abstract float getPressure();

  /**
   * Return battery charging status in Volt
   * 
   * @return
   */
  public abstract float getBattery();

  /**
   * Return battery charging status in Percent
   * 
   * @return
   */
  public abstract float getBatteryPercent();

  /**
   * Return a status messsage
   */
   public abstract String getStatus();

  /**
   * Return vertical speed in m/s
   */
  public abstract float getVerticalSpeed();

  /**
   * If we've been calibrated, override the GPS provided altitude with our aro based alt
   * @param l
   */
  public void improveLocation(Location l);
}