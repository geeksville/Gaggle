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
package com.geeksville.util;

/**
 * 
 * @author kevinh From: http://blueflyvario.blogspot.com/2011_05_01_archive.html
 *         A more simple approach is to use an IIR filter. This wikipedia
 *         article will confuse the hell out of most people. It is actually
 *         really easy when explained in words. You measure the altitude the
 *         first time. This becomes the 'current' altitude. With each subsequent
 *         measurement you sum X% of the new measurement with (100 - X)% of the
 *         previous 'current' altitude for the new 'current' altitude. This ends
 *         up being an exponential filter, where the most recent measurements
 *         have more weight than older measurements. The X% is the damping
 *         factor. Around 5 or 10 is about right, the lower the number the more
 *         damping. More damping equals more smoothness but more lag.
 */
public class IIRFilter {
  private float current = Float.NaN;

  // / Default to no averaging
  private float dampingFactor = 1.0f;

  public float getDampingFactor() {
    return dampingFactor;
  }

  public void setDampingFactor(float dampingFactor) {
    this.dampingFactor = dampingFactor;
  }

  public void addSample(float v) {
    if (Float.isNaN(current))
      current = v;
    else
      current = dampingFactor * v + (1 - dampingFactor) * current;
  }

  public float get() {
    return current;
  }
}
