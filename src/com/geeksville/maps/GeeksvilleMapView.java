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
package com.geeksville.maps;


import org.osmdroid.views.MapView;

import android.content.Context;
import android.util.AttributeSet;

public class GeeksvilleMapView extends MapView {

  private Runnable postLayout;

  public GeeksvilleMapView(Context context, AttributeSet attrs) {
    super(context, attrs);

    // this.getController().setZoom(this.getRenderer().ZOOM_MAXLEVEL); //
    // Start
    // off
    // zoomed
    // all
    // the
    // way
    // in

    // Start off somewhat zoomed in
    this.getController().setZoom(4);
  }

  /**
   * To work around OSM problems, we support calling a post layout callback
   * 
   * @see android.view.View#onLayout(boolean, int, int, int, int)
   */
  @Override
  protected void onLayout(boolean changed, int left, int top, int right,
      int bottom) {
    super.onLayout(changed, left, top, right, bottom);

    if (postLayout != null)
      postLayout.run();
  }

  public void setPostLayout(Runnable callback) {
    postLayout = callback;
  }

  /*	*//**
   * Work around for http://code.google.com/p/android/issues/detail?id=4599
   * 
   * @see android.view.ViewGroup#dispatchTouchEvent(android.view.MotionEvent)
   */
  /*
   * @Override public boolean dispatchTouchEvent(MotionEvent ev) { try { return
   * super.dispatchTouchEvent(ev); } catch (Exception ex) {
   * Log.e("GeeksvilleMapView", "error at zoom level " + this.getZoomLevel());
   * Log.e("GeeksvilleMapView", "Error in MapView:" +
   * Log.getStackTraceString(ex)); }
   * 
   * return true; }
   * 
   * 
   * Work around for http://code.google.com/p/android/issues/detail?id=4599
   * 
   * @see android.view.View#draw(android.graphics.Canvas)
   * 
   * @Override public void draw(Canvas canvas) { try { // TODO Auto-generated
   * method stub super.draw(canvas); } catch (Exception ex) {
   * Log.e("GeeksvilleMapView", ex.toString()); } }
   */

}
