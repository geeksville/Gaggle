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
package com.geeksville.test;

import com.geeksville.gaggle.R;
import com.geeksville.gaggle.R.drawable;
import com.geeksville.gaggle.R.id;
import com.geeksville.gaggle.R.layout;

import android.app.*;
import android.os.Bundle;
import android.widget.TabHost;

/**
 * FIXME - delete or move - I was playing with tabs, but tabs look like ass on android
 * @author kevinh
 *
 */
public class TabDemo extends TabActivity {
	

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	// FIXME - change to use a menu like the web browser app (instead of tabs)
    	
        setContentView(R.layout.tabs);

        TabHost tabs = (TabHost)this.findViewById(android.R.id.tabhost);
        tabs.setup();

        TabHost.TabSpec one = tabs.newTabSpec("one");
        one.setContent(R.id.content1);
        one.setIndicator("labelone", this.getResources().getDrawable(R.drawable.icon));
        // one.setIndicator("One");
        tabs.addTab(one);

        TabHost.TabSpec two = tabs.newTabSpec("two");
        two.setContent(R.id.content2);
        two.setIndicator("Two");
        tabs.addTab(two);

        tabs.setCurrentTab(0);
    	}
}
