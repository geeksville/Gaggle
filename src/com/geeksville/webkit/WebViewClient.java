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
