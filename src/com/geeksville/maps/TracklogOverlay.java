/**
 * 
 */
package com.geeksville.maps;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.PathOverlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;

import com.geeksville.location.LocationList;

/**
 * Overlay a tracklog plot over a map
 * 
 * @author kevinh
 * 
 */
public class TracklogOverlay extends PathOverlay {

	LocationList tracklog;

  public final int DETAIL_THRESHOLD = 1000; //the last n points that are drawn with full color detail 

	/**
	 * Max up/down millimeter/sec -- FIXME: set according to vario limits
	 */
	float maxMMeterSec = 5.0f, 
	      minMMeterSec = -5.0f;

	/**
	 * The colors we use for drawing our tracklog
	 */
	Paint[] trackPaints = new Paint[256];
	private ArrayList<Paint> mColors;

  static final int POINT_BUFFER_SIZE = 256; //should be a multiple of 4
  private float[] mPointBuffer = new float[4];
  private final Point mTempPoint1 = new Point();
  private final Point mTempPoint2 = new Point();
  private final Path mPath = new Path();
private List<Point> mPoints = new ArrayList<Point>();
  
	public TracklogOverlay(Context ctx, LocationList locs) {
		super(Color.RED, ctx);

		tracklog = locs;

		precalcTrackPaints();
	}

	private void precalcTrackPaints() {
	  for (int i=0;i<256;i++) {
	    Paint trackPaint = new Paint();
	    trackPaint.setColor(Color.rgb(255-Math.abs(127-i)*2, i, 255-i));  // blue -> red -> green
      trackPaint.setStyle(Style.STROKE);
      trackPaint.setStrokeWidth(2);
      trackPaints[i]=trackPaint;
	  }
	}

  @Override
  public void clearPath()
  {
      super.clearPath();
      this.mColors = new ArrayList<Paint>();
  }

  /**
   * This method draws the line.
   * Note - highly optimized to handle long paths, proceed with care. Should be fine up to 10K points.
   */
  @Override
  protected void draw(final Canvas canvas, final MapView mapView, final boolean shadow) 
  {
                if (this.tracklog.numPoints() < 2)
          {
                  //nothing to paint
                  return;
          }

          final Projection pj = mapView.getProjection();
          
          int size = this.tracklog.numPoints();
          if (size > mPoints.size()) {
            for (int i=getNumberOfPoints(); i<size; i++) {
              GeoPoint gpt = this.tracklog.getGeoPoint(i);
              Point pt = new Point(gpt.getLatitudeE6(), gpt.getLongitudeE6());
              pj.toMapPixelsProjected(pt.x, pt.y, pt); //convert to map projection
              mPoints.add(pt);
              if (i>0) {
                float rise = (tracklog.getAltitudeMM(i) - tracklog.getAltitudeMM(i-1)) / (tracklog.getTimeMsec(i) - tracklog.getTimeMsec(i-1)); // mm/ms = m/s
                rise       = Math.min(Math.max(rise,minMMeterSec),maxMMeterSec); // bound to limits
                float level  = (rise - minMMeterSec) / (maxMMeterSec - minMMeterSec) * 255.f;
                mColors.add(trackPaints[(int)level]);
              } else
                mColors.add(trackPaints[127]); 
            }
          }
                            
          Point screenPoint0 = null; //points on screen
          Point screenPoint1 = null;
          Point projectedPoint0; //points from the points list
          Point projectedPoint1;
          
          float [] buffer = this.mPointBuffer;
          int bufferCount = 0;            
          Rect clipBounds = pj.fromPixelsToProjected(canvas.getClipBounds()); // clipping rectangle in the intermediate projection, to avoid performing projection.
          Rect lineBounds = new Rect(); // bounding rectangle for the current line segment.
          
          mPath.rewind();
          projectedPoint0 = this.mPoints.get(size - 1);           
          lineBounds.set(projectedPoint0.x, projectedPoint0.y, projectedPoint0.x, projectedPoint0.y);

          boolean movedToStart = false;
          for(int i = size - 2; i >= 0; i--)
          {
                  //compute next points
                  projectedPoint1 = this.mPoints.get(i);
                  lineBounds.union(projectedPoint1.x, projectedPoint1.y);
                  
                  if (!Rect.intersects(clipBounds, lineBounds))
                  {
                          //skip this line, move to next point
                          projectedPoint0 = projectedPoint1;                              
                          screenPoint0 = null;
                          continue;
                  }
                  
                  // the starting point may be not calculated, because previous segment was out of clip bounds                    
                  if (screenPoint0 == null)
                  {
                          screenPoint0 = pj.toMapPixelsTranslated(projectedPoint0, this.mTempPoint1);
                  }
                                          
                  screenPoint1 = pj.toMapPixelsTranslated(projectedPoint1, this.mTempPoint2);
                  
                  //skip this point, too close to previous point
                  if (Math.abs(screenPoint1.x - screenPoint0.x) + Math.abs(screenPoint1.y - screenPoint0.y) <= 1)
                  {
                          continue;
                  }
                  
                  //add new line to buffer                        
                  
                  if (i > size - DETAIL_THRESHOLD) {                // if we are in the detailed range 
                      buffer[0] = screenPoint0.x;
                      buffer[1] = screenPoint0.y;
                      buffer[2] = screenPoint1.x;
                      buffer[3] = screenPoint1.y;
//                      bufferCount += 4;
                    canvas.drawLines(buffer, this.mColors.get(i));  // paint with accurate colors
//                    bufferCount = 0;
                  } else {
                    // path draws a lot faster than lines with supplied paint, so for the bulk-lines that are all red, we use a path:
                      if(!movedToStart){
                          movedToStart = true;
                          mPath.moveTo(screenPoint0.x, screenPoint0.y);
                      }
                      mPath.lineTo(screenPoint1.x, screenPoint1.y);
                  }
//                  if (bufferCount == POINT_BUFFER_SIZE) {           // else just paint in red
//                    canvas.drawLines(buffer, this.trackPaints[127]);
//                    bufferCount = 0;
//                  }                               
                  //update starting point to next position 
                  projectedPoint0 = projectedPoint1;                      
                  screenPoint0.x = screenPoint1.x;
                  screenPoint0.y = screenPoint1.y;                        
                  lineBounds.set(projectedPoint0.x, projectedPoint0.y, projectedPoint0.x, projectedPoint0.y);                     
          }
          canvas.drawPath(mPath, this.trackPaints[127]);

//          if (bufferCount >0 )
//          {
//                  canvas.drawLines(buffer, 0,  bufferCount, this.mPaint);
//          }
  }
	

}
