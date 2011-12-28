package com.geeksville.gaggle;

import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.media.AudioTrack;
import android.media.AudioTrack.OnPlaybackPositionUpdateListener;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.geeksville.android.TonePlayer;
import com.geeksville.location.BarometerClient;

public class AudioVario implements Observer, Runnable {

  private static final String TAG = "AudioVario";

  private TonePlayer liftTone = new TonePlayer(880f);
  private TonePlayer sinkTone = new TonePlayer(220f);
  private TonePlayer curTone = liftTone;

  private BarometerClient baro;
  Handler handler;

  /** Or -1 for disabled */
  long beepDelayMsec = -1;
  boolean isPlaying = false;

  float minSinkThreshold = 2.0f;
  float maxSinkThreshold = 4.0f;
  float minSinkHz = 1f, maxSinkHz = 10f;

  float minLiftThreshold = 0.5f;
  float maxLiftThreshold = 4.0f;
  float minLiftHz = 1f, maxLiftHz = 10f;

  /**
   * @see com.geeksville.info.InfoField#onCreate(android.app.Activity)
   */
  public void onCreate(Context context, Looper looper) {
    if (context != null) {
      handler = new Handler(looper);

      // FIXME - we should share one compass client object
      baro = BarometerClient.create(context);
      if (baro != null)
        baro.addObserver(this);

      testTones();
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

  public void testTones() {
    Thread t = new Thread(new Runnable() {

      @Override
      public void run() {
        // TODO Auto-generated method stub
        for (float f = -maxSinkThreshold; f <= maxLiftThreshold; f += 0.2f) {
          updateTone(f);
          try {
            Thread.sleep(2 * 1000);
          } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      }
    }, "TestTones");

    t.start();
  }

  private static float interpolate(float x0, float x1, float y0, float y1,
      float x) {
    return y0 + ((x - x0) * y1 - (x - x0) * y0) / (x1 - x0);
  }

  private void updateTone(float vs) {

    float beepHz;
    if (vs >= minLiftThreshold) {
      if (vs > maxLiftThreshold)
        vs = maxLiftThreshold; // clamp

      beepHz = interpolate(minLiftThreshold, maxLiftThreshold, minLiftHz,
          maxLiftHz, vs);
      curTone = liftTone;
    } else if (vs <= -minSinkThreshold) {
      if (vs < -maxSinkThreshold)
        vs = maxSinkThreshold; // clamp

      beepHz = interpolate(minSinkThreshold, maxSinkThreshold, minSinkHz,
          maxSinkHz, -vs);
      curTone = sinkTone;
    } else
      beepHz = -1f; // Turn tone off

    beepDelayMsec = (int) (1000 / beepHz);
    Log.d(TAG, "vs=" + vs + " -> hz=" + beepHz + " delay=" + beepDelayMsec);
    startTone();
  }

  private synchronized void startTone() {
    if (beepDelayMsec > 0) {
      // Prime the pump if necessary
      if (!isPlaying) {
        isPlaying = true;
        curTone.play();

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
