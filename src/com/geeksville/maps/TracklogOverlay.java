/**
 * 
 */
package com.geeksville.maps;

import java.util.ArrayList;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapView.OpenStreetMapViewProjection;
import org.andnav.osm.views.overlay.OpenStreetMapViewPathOverlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.PaintDrawable;
import android.util.Log;

import com.geeksville.location.LocationList;

/**
 * Overlay a tracklog plot over a map
 * 
 * @author kevinh
 * 
 */
public class TracklogOverlay extends OpenStreetMapViewPathOverlay {

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
  private ArrayList<Point> mPoints;
	private ArrayList<Paint> mColors;

  static final int POINT_BUFFER_SIZE = 256; //should be a multiple of 4
  private float[] mPointBuffer = new float[POINT_BUFFER_SIZE];
  private final Point mTempPoint1 = new Point();
  private final Point mTempPoint2 = new Point();
  
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
      this.mPoints = new ArrayList<Point>();
      this.mColors = new ArrayList<Paint>();
  }

  /**
   * This method draws the line.
   * Note - highly optimized to handle long paths, proceed with care. Should be fine up to 10K points.
   */
  @Override
  protected void onDraw(Canvas canvas, OpenStreetMapView mapView) 
  {
          if (this.tracklog.numPoints() < 2)
          {
                  //nothing to paint
                  return;
          }

          final OpenStreetMapViewProjection pj = mapView.getProjection();
          
          int size = this.tracklog.numPoints();
          if (size > mPoints.size()) {
            for (int i=mPoints.size(); i<size; i++) {
              GeoPoint gpt = this.tracklog.getGeoPoint(i);
              Point pt = new Point(gpt.getLatitudeE6(), gpt.getLongitudeE6());
              pj.toMapPixelsProjected(pt.x, pt.y, pt); //convert to map projection
              mPoints.add(pt);
              if (i>0) {
            	int deltaTm =   tracklog.getTimeMsec(i) - tracklog.getTimeMsec(i-1);
            	if (deltaTm != 0) {
	                float rise = (tracklog.getAltitudeMM(i) - tracklog.getAltitudeMM(i-1)) / deltaTm; // mm/ms = m/s
	                rise       = Math.min(Math.max(rise,minMMeterSec),maxMMeterSec); // bound to limits
	                float level  = (rise - minMMeterSec) / (maxMMeterSec - minMMeterSec) * 255.f;
	                mColors.add(trackPaints[(int)level]);
	           	} else
            		mColors.add(trackPaints[127]);
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
          
          projectedPoint0 = this.mPoints.get(size - 1);           
          lineBounds.set(projectedPoint0.x, projectedPoint0.y, projectedPoint0.x, projectedPoint0.y);

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
                  buffer[bufferCount] = screenPoint0.x;
                  buffer[bufferCount + 1] = screenPoint0.y;
                  buffer[bufferCount + 2] = screenPoint1.x;
                  buffer[bufferCount + 3] = screenPoint1.y;
                  bufferCount += 4;
                  
                  if (i > size - DETAIL_THRESHOLD) {                // if we are in the detailed range 
                    canvas.drawLines(buffer, this.mColors.get(i));  // paint with accurate colors
                    bufferCount = 0;
                  } else 
                  if (bufferCount == POINT_BUFFER_SIZE) {           // else just paint in red
                    canvas.drawLines(buffer, this.trackPaints[127]);
                    bufferCount = 0;
                  }                               
                  
                  //update starting point to next position 
                  projectedPoint0 = projectedPoint1;                      
                  screenPoint0.x = screenPoint1.x;
                  screenPoint0.y = screenPoint1.y;                        
                  lineBounds.set(projectedPoint0.x, projectedPoint0.y, projectedPoint0.x, projectedPoint0.y);                     
          }

          if (bufferCount >0 )
          {
                  canvas.drawLines(buffer, 0,  bufferCount, this.mPaint);
          }
  }
	

}
