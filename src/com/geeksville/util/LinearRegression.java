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
    public float normx;

    public Sample(long x, float y) {
      this.x = x;
      this.y = y;
      this.normx = 0; //will be recalculated
    }
  }

  private Queue<Sample> samples = new ArrayDeque<Sample>();

  private long xspan = 2 * 1000; // typically milliseconds

  public float getXspan() {
    return xspan;
  }

  // / We will keep only the most recent yspan interval around
  public void setXspan(long xspan) {
    this.xspan = xspan;
  }

  public void addSample(long x, float y) {
	  //shouldn't happen, but don't let it pollute the sample pool if somehow this triggers
	  if(Float.isNaN(y))
	  {
		  assert(false);
		  return;
	  }
	  
    synchronized (samples) {
      {
        Sample s = new Sample(x, y);
        samples.add(s);
      }

      // Cull old entries
      long oldest = x - xspan;
      while (samples.peek().x < oldest) {
        Sample s = samples.remove();
      }
    }
  }

  public double getSlope() {
    synchronized (samples) {
    	///have some reasonable amount of samples
    	if(samples.size() < 2)
    		return 0;
    	
    	long basex = samples.peek().x;
    	
    	double sumx = 0;
    	double sumy = 0;
    	
    	for (Sample s : samples)
    	{
        	//arithmetic on raw timestamp (in ms) is fairly dangerous, reduce
    		//down to a normalized form (offset from the first item)
    		s.normx = (float) (s.x - basex);
    		sumx += s.normx;
    		sumy += s.y;
    	}
    	
    	double xbar = sumx / samples.size();
    	double ybar = sumy / samples.size();
    	double xxbar = 0.0, xybar = 0.0;
    	for (Sample s : samples) {
    		xxbar += (s.normx - xbar) * (s.normx - xbar);
    		xybar += (s.normx - xbar) * (s.y - ybar);
    	}
    	
    	if(xxbar > 0.0001)
    	{
	    	double beta1 = xybar / xxbar;
	    	return beta1;
    	}
    	else
    	{
    		return 0;
    	}
    }
  }
}
