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

import java.util.HashSet;
import java.util.Set;

public class LifeCyclePublisherImpl implements LifeCyclePublisher, LifeCycleHandler {

	Set<LifeCycleHandler> handlers = new HashSet<LifeCycleHandler>();

	private enum LifeState {
		Virgin, Started, Resumed, Paused, Stopped
	};

	LifeState curState = LifeState.Virgin;

	@Override
	public void addLifeCycleHandler(LifeCycleHandler h) {
		handlers.add(h);

		// If handlers are added after we are already up and running (not
		// stopped), advance them to the correct
		// state
		if (curState.ordinal() >= LifeState.Started.ordinal())
			h.onStart();
		if (curState.ordinal() >= LifeState.Resumed.ordinal())
			h.onResume();
		if (curState.ordinal() >= LifeState.Paused.ordinal())
			h.onPause();
		if (curState.ordinal() >= LifeState.Stopped.ordinal())
			h.onStop();
	}

	@Override
	public void removeLifeCycleHandler(LifeCycleHandler h) {
		handlers.remove(h);

		// FIXME - should I claim stopped?
	}

	@Override
	public void onPause() {
		curState = LifeState.Paused;

		for (LifeCycleHandler h : handlers)
			h.onPause();
	}

	@Override
	public void onResume() {
		curState = LifeState.Resumed;

		for (LifeCycleHandler h : handlers)
			h.onResume();
	}

	@Override
	public void onStart() {
		curState = LifeState.Started;

		for (LifeCycleHandler h : handlers)
			h.onStart();
	}

	@Override
	public void onStop() {
		curState = LifeState.Stopped;

		for (LifeCycleHandler h : handlers)
			h.onStop();
	}
}
