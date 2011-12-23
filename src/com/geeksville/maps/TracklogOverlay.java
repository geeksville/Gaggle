/**
 * 
 */
package com.geeksville.maps;

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

import com.geeksville.location.LocationList;

/**
 * Overlay a tracklog plot over a map
 * 
 * @author kevinh
 * 
 */
public class TracklogOverlay extends OpenStreetMapViewPathOverlay {

	LocationList tracklog;

	/**
	 * Max up/down millimeter/sec we've seen so far
	 */
	int maxMMeterSec, minMMeterSec;

	/**
	 * The color we use for drawing our tracklog
	 */
	Paint trackPaint = new Paint();

	/**
	 * 256 colors, 0th position is 'most down' and 255th position is 'most up'
	 */
	int[] hsvMap = new int[256];

	public TracklogOverlay(Context ctx, LocationList locs) {
		super(Color.RED, ctx);

		tracklog = locs;

		trackPaint.setStyle(Style.STROKE);
		trackPaint.setStrokeWidth(2);

		calcHSVMap();
	}

	private void calcHSVMap() {
		float[] hsv = { 1.0f, 1.0f, 1.0f };

		for (int i = 0; i < hsvMap.length; i++) {
			hsv[0] = (float) i * 360 / hsvMap.length;

			hsvMap[i] = Color.HSVToColor(hsv);
		}
	}

	/**
	 * Draw a red line for our tracklog
	 * 
	 * @see Overlay#draw(android.graphics.Canvas, boolean)
	 */
	public void fixmeBustedOnOSM(Canvas canvas, OpenStreetMapView mapView) {

		OpenStreetMapViewProjection proj = mapView.getProjection();

		Point p = new Point();
		int prevZ = 0, prevTime = 0;
		float prevX = 0, prevY = 0;
		boolean hasPrev = false;

		int[] times = tracklog.timeMsec.toUnsafeArray();
		int[] alts = tracklog.altitudeMM.toUnsafeArray();

		// Provide rational initial condition
		if (tracklog.numPoints() > 0) {
			prevZ = alts[0];
			prevTime = times[0];
		}

		int hue = hsvMap.length / 2; // Default to something middling
		for (int i = 0; i < tracklog.numPoints(); i++) {
			GeoPoint gp = tracklog.getGeoPoint(i);
			int curTime = times[i];
			// for time
			// FIXME - busted on OSM
			// proj.toPixels(gp, p);

			if (hasPrev) {
				int deltaMsec = curTime - prevTime;

				// Integrate over the last X seconds of time
				int integrateMsec = 10 * 1000;

				if (deltaMsec > integrateMsec) {
					int curZ = alts[i];
					int deltaMM = curZ - prevZ;
					int mmPerSecUp = (deltaMM * 1000) / deltaMsec;

					if (mmPerSecUp > maxMMeterSec)
						maxMMeterSec = mmPerSecUp;

					if (mmPerSecUp < minMMeterSec)
						minMMeterSec = mmPerSecUp;

					// linear interpolate between min and max vert speed, 1.0==
					// max
					hue = (hsvMap.length - 1) * (mmPerSecUp - minMMeterSec)
							/ (maxMMeterSec - minMMeterSec);

					// Scale hue so max up is yellow, and max down is blue
					if (hue >= hsvMap.length)
						hue = hsvMap.length - 1;
					else if (hue < 0)
						hue = 0;

					prevTime = curTime;
					prevZ = curZ;
					// FIXME, we should do some sort of moving boxcar average
					// instead
				}

				// trackPaint.setARGB(255, 255, 0, 0);
				int color = hsvMap[hue];

				trackPaint.setColor(color);

				// FIXME - for live flights we should only plot a detailed
				// tracklog for the last
				// 20 minutes. Prior to that we
				// can skip points

				// FIXME, the array based versions are probably faster
				canvas.drawLine(prevX, prevY, p.x, p.y, trackPaint);
			}

			prevX = p.x;
			prevY = p.y;
			hasPrev = true;
		}
	}

	/**
	 * Until I can customize the OSM code, convert to their format
	 */
	private void convertToOSM() {
		int mycount = tracklog.numPoints();
		int osmCount = getNumberOfPoints();

		if (mycount < osmCount) {
			clearPath();
			osmCount = 0;
		}

		for (int i = osmCount; i < mycount; i++)
			addPoint(tracklog.getGeoPoint(i));
	}

	/**
	 * Convert our points into OSM format then call their drawer
	 * 
	 * @see org.andnav.osm.views.overlay.OpenStreetMapViewPathOverlay#onDraw(android.graphics.Canvas,
	 *      org.andnav.osm.views.OpenStreetMapView)
	 */
	@Override
	protected void onDraw(Canvas canvas, OpenStreetMapView mapView) {

		convertToOSM();

		super.onDraw(canvas, mapView);
	}

}
