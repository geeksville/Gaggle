package com.geeksville.maps;

import java.util.Timer;
import java.util.TimerTask;

import org.osmdroid.api.IGeoPoint;
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

	private void resetMapChangeTimer()
	{
		mChangeDelayTimer.cancel();
		mChangeDelayTimer = new Timer();
		mChangeDelayTimer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				if (mChangeListener != null){
					Log.d("ScrollMap", "dispatching scroll");
					mChangeListener.onChange(mThis, getMapCenter(), 
							mLastCenterPosition, getZoomLevel(), mLastZoomLevel);
				}
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