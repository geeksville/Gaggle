package com.geeksville.maps;

import java.util.Timer;
import java.util.TimerTask;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class ScrollChangeMapView extends MapView {
	// ------------------------------------------------------------------------
	// LISTENER DEFINITIONS
	// ------------------------------------------------------------------------

	// Change listener
	public static interface OnChangeListener
	{
		public void onChange(MapView view, IGeoPoint newCenter, IGeoPoint oldCenter, int newZoom, int oldZoom);
	}

	private ScrollChangeMapView mThis;
	private long mEventsTimeout = 250L;     // Set this variable to your preferred timeout
	private boolean mIsTouched = false;
	private IGeoPoint mLastCenterPosition;
	private int mLastZoomLevel;
	private Timer mChangeDelayTimer = new Timer();
	private ScrollChangeMapView.OnChangeListener mChangeListener = null;

	public ScrollChangeMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init()
	{
		mThis = this;
		mLastCenterPosition = this.getMapCenter();
		mLastZoomLevel = this.getZoomLevel();
	}

	private class ScrollChangeMapController extends MapController {

		public ScrollChangeMapController(MapView mapview){
			super(mapview);
		}

		@Override
		public void setCenter(IGeoPoint point) {
			super.setCenter(point);
			notifyListeners();
		}

		@Override
		public int setZoom(int zoomlevel) {
			final int z = super.setZoom(zoomlevel);
			notifyListeners();
			return z;
		}

		@Override
		public boolean zoomIn() {
			final boolean z = super.zoomIn();
			notifyListeners();
			return z;
		}

		@Override
		public boolean zoomInFixing(int xPixel, int yPixel) {
			final boolean z = super.zoomInFixing(xPixel, yPixel);
			notifyListeners();
			return z;
		}

		@Override
		public boolean zoomOut() {
			final boolean z = super.zoomOut();
			notifyListeners();
			return z;
		}

		@Override
		public boolean zoomOutFixing(int xPixel, int yPixel) {
			final boolean z = super.zoomOutFixing(xPixel, yPixel);
			notifyListeners();
			return z;
		}

		@Override
		public void zoomToSpan(int reqLatSpan, int reqLonSpan) {
			super.zoomToSpan(reqLatSpan, reqLonSpan);
			notifyListeners();
		}

		@Override
		public void animateTo(IGeoPoint point) {
			super.animateTo(point);
			notifyListeners();
		}
	}

	private MapController singletonCtrl;

	@Override
	public MapController getController(){
		if (singletonCtrl == null)
			singletonCtrl = new ScrollChangeMapController(this);
		return singletonCtrl;
	}

	// ------------------------------------------------------------------------
	// GETTERS / SETTERS
	// ------------------------------------------------------------------------

	public void setOnScrollChangeListener(ScrollChangeMapView.OnChangeListener l)
	{
		mChangeListener = l;
	}

	public ScrollChangeMapView.OnChangeListener getOnScrollChangeListener()
	{
		return mChangeListener;
	}

	// ------------------------------------------------------------------------
	// EVENT HANDLERS
	// ------------------------------------------------------------------------

	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		// Set touch internal
		mIsTouched = (ev.getAction() != MotionEvent.ACTION_UP);

		return super.onTouchEvent(ev);
	}

	@Override
	public void computeScroll()
	{
		super.computeScroll();

		// Check for change
		if (isSpanChange() || isZoomChange())
		{
			// If computeScroll called before timer counts down we should drop it and
			// start counter over again
			resetMapChangeTimer();
		}
	}


	// ------------------------------------------------------------------------
	// TIMER RESETS
	// ------------------------------------------------------------------------

	public void notifyListeners(){
		if (mChangeListener != null){
			mChangeListener.onChange(mThis, getMapCenter(), 
					mLastCenterPosition, getZoomLevel(), mLastZoomLevel);
		}
	}

	private void resetMapChangeTimer()
	{
		mChangeDelayTimer.cancel();
		mChangeDelayTimer = new Timer();
		mChangeDelayTimer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{

				notifyListeners();

				mLastCenterPosition = getMapCenter();
				mLastZoomLevel = getZoomLevel();
			}
		}, mEventsTimeout);
	}
    // ------------------------------------------------------------------------
    // CHANGE FUNCTIONS
    // ------------------------------------------------------------------------
 
    private boolean isSpanChange()
    {
        return !mIsTouched && !getMapCenter().equals(mLastCenterPosition);
    }
 
    private boolean isZoomChange()
    {
        return (getZoomLevel() != mLastZoomLevel);
    }
}