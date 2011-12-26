package com.geeksville.gaggle;

import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.media.AudioTrack;
import android.media.AudioTrack.OnPlaybackPositionUpdateListener;

import com.geeksville.android.TonePlayer;
import com.geeksville.location.BarometerClient;

public class AudioVario implements Observer, OnPlaybackPositionUpdateListener {

	private TonePlayer tone = new TonePlayer();
	private BarometerClient baro;
	private float vs = 0.0f;

	/**
	 * @see com.geeksville.info.InfoField#onCreate(android.app.Activity)
	 */
	public void onCreate(Context context) {
		if (context != null) {
			// FIXME - we should share one compass client object
			baro = BarometerClient.create(context);
			baro.addObserver(this);

			tone.play();
		}
	}

	/**
	 * @see com.geeksville.info.InfoField#onHidden()
	 */
	public void onDestroy() {
		if (baro != null)
			baro.deleteObserver(this);
	}

	@Override
	public void update(Observable observable, Object data) {
		vs = baro.getVerticalSpeed();
		updateTone();
	}

	private void updateTone() {
		float minSinkThreshold = 2.0f;
		float minLiftThreshold = 0.5f;
		fixme
	}

	@Override
	public void onMarkerReached(AudioTrack track) {
		// Start with new settings
		fixme
	}

	@Override
	public void onPeriodicNotification(AudioTrack track) {
		// Ignore
	}
}
