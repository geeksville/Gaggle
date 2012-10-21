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