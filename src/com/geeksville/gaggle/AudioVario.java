/****************************************************************************************
 * Gaggle is Copyright 2010, 2011, and 2012 by Kevin Hester of Geeksville Industries LLC,
 * a California limited liability corporation. 
 * 
 * Gaggle is free software: you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation, either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * Gaggle is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE.  See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with Gaggle 
 * included in this distribution in the manual (assets/manual/gpl-v3.txt). If not, see  
 * <http://www.gnu.org/licenses/> or at <http://gplv3.fsf.org>.
 ****************************************************************************************/
package com.geeksville.gaggle;

import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import com.geeksville.android.PreferenceUtil;
import com.geeksville.android.TonePlayer;
import com.geeksville.location.BarometerClient;
import com.geeksville.location.IBarometerClient;

public class AudioVario implements Observer, Runnable,
    SharedPreferences.OnSharedPreferenceChangeListener {

  private static final String TAG = "AudioVario";

  private TonePlayer liftTone;
  private TonePlayer sinkTone;
  private TonePlayer curTone;

  private IBarometerClient baro;
  Handler handler;

  /** Or <0 for disabled */
  long beepDelayMsec = -1;
  boolean isPlaying = false;

  float minSinkThreshold = 2.0f;
  float maxSinkThreshold = 4.0f;
  float minSinkHz = 1f, maxSinkHz = 10f;

  float minLiftThreshold = 0.5f;
  float maxLiftThreshold = 4.0f;
  float minLiftHz = 1f, maxLiftHz = 10f;

  private Context context;

  /**
   * @see com.geeksville.info.InfoField#onCreate(android.app.Activity)
   */
  public void onCreate(Context context, Looper looper) {
    this.context = context;

    if (context != null) {

      handler = new Handler(looper);

      PreferenceManager.getDefaultSharedPreferences(context)
          .registerOnSharedPreferenceChangeListener(this);

      createFromPreferences();
    }
  }

  private void createFromPreferences() {
    onDestroy(); // Tear down old devices

    if (PreferenceUtil.getBoolean(context, "use_audible_vario", true)) {

      liftTone = new TonePlayer(PreferenceUtil.getFloat(context, "liftTone2",
          1100f));
      sinkTone = new TonePlayer(PreferenceUtil.getFloat(context, "sinkTone",
          220f));

      minSinkThreshold = PreferenceUtil.getFloat(context, "minSinkThreshold",
          2.0f);
      maxSinkThreshold = PreferenceUtil.getFloat(context, "maxSinkThreshold",
          4.0f);
      minSinkHz = PreferenceUtil.getFloat(context, "minSinkHz", 1f);
      maxSinkHz = PreferenceUtil.getFloat(context, "maxSinkHz", 10f);

      minLiftThreshold = PreferenceUtil.getFloat(context, "minLiftThreshold",
          0.5f);
      maxLiftThreshold = PreferenceUtil.getFloat(context, "maxLiftThreshold",
          4.0f);
      minLiftHz = PreferenceUtil.getFloat(context, "minLiftHz", 1f);
      maxLiftHz = PreferenceUtil.getFloat(context, "maxLiftHz", 10f);

      baro = BarometerClient.create(context);
      if (baro != null)
        baro.addObserver(this);

      // testTones();
    }
  }

  /**
   * @see com.geeksville.info.InfoField#onHidden()
   */
  public void onDestroy() {
    if (baro != null)
      baro.deleteObserver(this);
    if (liftTone != null)
      liftTone.close();
    if (sinkTone != null)
      sinkTone.close();
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

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
      String key) {
    createFromPreferences();
  }

}
