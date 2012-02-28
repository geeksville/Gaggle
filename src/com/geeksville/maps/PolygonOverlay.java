package com.geeksville.maps;
import java.util.ArrayList;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;

/**
 * Overlay for osmdroid for drawing Polygon
 * @author poulhies
 */
public class PolygonOverlay extends Overlay {

	public static class GeoPolygon {
		public ArrayList<GeoPoint> mPoints = new ArrayList<GeoPoint>();
	}

	private static class Polygon {
		public final ArrayList<Point> mPoints = new ArrayList<Point>();
		public int mPointsPrecomputed = 0;
		public final Path mPath = new Path();
		private final Rect mLineBounds = new Rect();
	}

	private ArrayList<Polygon> mPolygons;
	protected Paint mPaint = new Paint();

	protected Paint mPolyPaint = new Paint();

	private final Point mTempPoint1 = new Point();
	private final Point mTempPoint2 = new Point();

	public PolygonOverlay(final Context ctx) {
		this(new DefaultResourceProxyImpl(ctx));
	}

	public void addPolygon(final GeoPolygon poly){
		final Polygon new_p = new Polygon();

		for (GeoPoint gp : poly.mPoints){
			new_p.mPoints.add(new Point(gp.getLatitudeE6(), gp.getLongitudeE6()));
		}
		mPolygons.add(new_p);
	}

	public PolygonOverlay(final ResourceProxy pResourceProxy) {
		super(pResourceProxy);

		this.mPolyPaint.setColor(android.graphics.Color.RED);
		this.mPolyPaint.setStyle(Paint.Style.FILL);
		this.mPolyPaint.setStrokeWidth(3);
		this.mPolyPaint.setAlpha(90);

		clearPolys();
	}

	public void clearPolys(){
		this.mPolygons = new ArrayList<Polygon>();
	}

	private void drawPath(Polygon poly, Projection pj, Canvas canvas){
		final ArrayList<Point> points = poly.mPoints;

		if (points.size() < 2)
			return;

		// precompute new points to the intermediate projection.
		final int size = points.size();

		while (poly.mPointsPrecomputed < size) {
			final Point pt = points.get(poly.mPointsPrecomputed);
			pj.toMapPixelsProjected(pt.x, pt.y, pt);
			poly.mPointsPrecomputed++;
		}

		Point screenPoint0 = null; // points on screen
		Point screenPoint1 = null;
		Point projectedPoint0; // points from the points list
		Point projectedPoint1;

		// clipping rectangle in the intermediate projection, to avoid performing projection.
		final Rect clipBounds = pj.fromPixelsToProjected(pj.getScreenRect());

		poly.mPath.rewind();
		projectedPoint0 = points.get(size - 1);
		poly.mLineBounds.set(projectedPoint0.x, projectedPoint0.y, projectedPoint0.x, projectedPoint0.y);

		/*
		 * This comes from osmdroid's PathOverlay This is not correct for
		 * polygon drawing when some side are outside of the screen.
		 */
		for (int i = size - 2; i >= 0; i--) {
			// compute next points
			projectedPoint1 = points.get(i);
			poly.mLineBounds.union(projectedPoint1.x, projectedPoint1.y);

// FIXME: this opt only works for linestring.
// It should be modified for polys (f-e: compute poly bbox beforehand)
//			if (!Rect.intersects(clipBounds, poly.mLineBounds)) {
//				// skip this line, move to next point
//				projectedPoint0 = projectedPoint1;
//				screenPoint0 = null;
//				continue;
//			}

			// the starting point may be not calculated, because previous segment was out of clip
			// bounds
			if (screenPoint0 == null) {
				screenPoint0 = pj.toMapPixelsTranslated(projectedPoint0, this.mTempPoint1);
				poly.mPath.moveTo(screenPoint0.x, screenPoint0.y);
			}

			screenPoint1 = pj.toMapPixelsTranslated(projectedPoint1, this.mTempPoint2);

			// skip this point, too close to previous point
			if (Math.abs(screenPoint1.x - screenPoint0.x)
					+ Math.abs(screenPoint1.y - screenPoint0.y) <= 1) {
				continue;
			}

			poly.mPath.lineTo(screenPoint1.x, screenPoint1.y);

			// update starting point to next position
			projectedPoint0 = projectedPoint1;
			screenPoint0.x = screenPoint1.x;
			screenPoint0.y = screenPoint1.y;
			poly.mLineBounds.set(projectedPoint0.x, projectedPoint0.y, projectedPoint0.x,
					projectedPoint0.y);
		}
		poly.mPath.close();
		canvas.drawPath(poly.mPath, this.mPolyPaint);
	}

	@Override
	protected void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
		if (shadow){
			return;
		}
		if (this.mPolygons.isEmpty()) {
			// nothing to paint
			return;
		}

		final Projection pj = mapView.getProjection();

		for (Polygon p :  this.mPolygons){
			drawPath(p, pj, canvas);
		}
	}
}