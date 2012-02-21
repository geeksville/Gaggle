package com.geeksville.maps;
import java.util.ArrayList;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;

public class PolygonOverlay extends Overlay {

	public class GeoPolygon {
		public ArrayList<GeoPoint> mPoints = new ArrayList<GeoPoint>();
	}
	
	private class Polygon {
		public final ArrayList<Point> mPoints = new ArrayList<Point>();
		public final Path mPath = new Path();
	}
	
	private ArrayList<Polygon> mPolygons;
    protected Paint mPaint = new Paint();

	public PolygonOverlay(final Context ctx) {
		this(new DefaultResourceProxyImpl(ctx));
	}

	public void addPolygon(final GeoPolygon poly){
		Polygon new_p = new Polygon();
		for (GeoPoint gp : poly.mPoints){
			new_p.mPoints.add(new Point(gp.getLatitudeE6(), gp.getLatitudeE6()));
		}
		mPolygons.add(new_p);
	}
	
	public PolygonOverlay(final ResourceProxy pResourceProxy) {
		super(pResourceProxy);
	}

	@Override
	protected void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
		if (shadow){
			return;
		}


	}
}
