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
package com.geeksville.webkit;

import android.webkit.WebView;

/**
 * 
 * @author Dawson
 * 
 *	On some devices ( Samsung Galaxy S  & possibly some Droids running Froyo)
 *	reloative links in a html page loaded in a webview object using code such 
 *	as webview.loadUrl("file:///android_asset/....."); or absolute links using 
 *	href="file://android_asset/.....") result in a page not found.
 *	It's as though the browsers's context is not maintaned as the apps context,
 *	and instead context is localised to the browser after the webkit is loaded.
 *	By overriding the URLLoading event, It seems as though the links are able 
 *	to target the correct context, and therefore understand where to find the 
 *	assets. 
 */ 
public class WebViewClient extends android.webkit.WebViewClient {
	@Override  
	  public boolean shouldOverrideUrlLoading(WebView view, String url)
	  {  
	    view.loadUrl(url);
	    return true;
	  } 
}
