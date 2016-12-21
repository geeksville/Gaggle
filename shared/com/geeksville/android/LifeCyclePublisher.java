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
package com.geeksville.android;

/**
 * Allows subscribers to find out about onPause/onStart etc...
 * 
 * @author kevinh
 * 
 *         I have a situation where a number of my components would like to turn
 *         off CPU using features while not visible. Unfortunately onPause isn't
 *         sent to Views. I would just make my own subclass of Activity which
 *         adds this feature, but I want to use MapActivity and therefore can't
 *         just use straight subclassing. If you want to find out about onPause
 *         etc... have your activity implement this interface (using
 *         LifeCyclePublisherImpl to do the dirty work). Then your various
 *         components can go to ((LifeCyclePublisher) getContext()) and be
 *         happy.
 */
public interface LifeCyclePublisher {

	public void addLifeCycleHandler(LifeCycleHandler h);

	public void removeLifeCycleHandler(LifeCycleHandler h);
}
