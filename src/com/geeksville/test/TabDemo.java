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
