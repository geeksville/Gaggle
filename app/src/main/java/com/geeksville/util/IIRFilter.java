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
