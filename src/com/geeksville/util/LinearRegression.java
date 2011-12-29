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

import java.util.ArrayDeque;
import java.util.Queue;

public class LinearRegression {

  static class Sample {
    public long x;
    public float y;

    public Sample(long x, float y) {
      this.x = x;
      this.y = y;
    }
  }

  private Queue<Sample> samples = new ArrayDeque<Sample>();

  // / Invariants
  long sumx;
  double sumy;

  private long xspan = 2 * 1000; // typically milliseconds

  public float getXspan() {
    return xspan;
  }

  // / We will keep only the most recent yspan interval around
  public void setXspan(long xspan) {
    this.xspan = xspan;
  }

  public void addSample(long x, float y) {
    synchronized (samples) {
      {
        Sample s = new Sample(x, y);
        sumx += x;
        sumy += y;
        samples.add(s);
      }

      // Cull old entries
      long oldest = x - xspan;
      while (samples.peek().x < oldest) {
        Sample s = samples.remove();
        sumx -= s.x;
        sumy -= s.y;
      }
    }
  }

  public float getSlope() {
    synchronized (samples) {
      double xbar = sumx / samples.size();
      double ybar = sumy / samples.size();
      double xxbar = 0.0, xybar = 0.0;
      for (Sample s : samples) {
        xxbar += (s.x - xbar) * (s.x - xbar);
        xybar += (s.x - xbar) * (s.y - ybar);
      }
      double beta1 = xybar / xxbar;

      return (float) beta1;
    }
  }
}
