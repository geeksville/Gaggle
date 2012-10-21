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
