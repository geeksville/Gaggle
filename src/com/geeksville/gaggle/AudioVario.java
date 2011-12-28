package com.geeksville.gaggle;

import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.media.AudioTrack;
import android.media.AudioTrack.OnPlaybackPositionUpdateListener;
import android.os.Handler;
import android.os.Looper;

import com.geeksville.android.TonePlayer;
import com.geeksville.location.BarometerClient;

public class AudioVario implements Observer, Runnable {

  private TonePlayer liftTone = new TonePlayer();
  private BarometerClient baro;
  Handler handler;

  /** Or -1 for disabled */
  long beepDelayMsec = -1;
  boolean isPlaying = false;

  /**
   * @see com.geeksville.info.InfoField#onCreate(android.app.Activity)
   */
  public void onCreate(Context context, Looper looper) {
    if (context != null) {
      handler = new Handler(looper);

      // FIXME - we should share one compass client object
      baro = BarometerClient.create(context);
      baro.addObserver(this);
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
    updateTone(baro.getVerticalSpeed());
  }

  private void updateTone(float vs) {
    float minSinkThreshold = 2.0f;
    float minLiftThreshold = 0.5f;
    float maxLiftThreshold = 4.0f;
    float minLiftHz = 1f, maxLiftHz = 0.2f;

    if (vs >= minLiftThreshold) {
      if (vs > maxLiftThreshold)
        vs = maxLiftThreshold; // clamp

      // linear interpolate
      float beepHz = minLiftHz
          + ((vs - minLiftThreshold) * maxLiftHz - (vs - minLiftThreshold)
              * minLiftHz) / (maxLiftThreshold - minLiftThreshold);

      beepDelayMsec = (int) (1000 / beepHz);
      startTone();
    }
  }

  private synchronized void startTone() {
    if (beepDelayMsec > 0) {
      // Prime the pump if necessary
      if (!isPlaying) {
        isPlaying = true;
        liftTone.play();

        // Start the new delay
        handler.postDelayed(this, beepDelayMsec);
      }
    }
  }

  /** Called when our timer fires */
  @Override
  public synchronized void run() {
    // Queue up the next event
    isPlaying = false;
    startTone();
  }

}
