package com.geeksville.android;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class TonePlayer {

  // originally from
  // http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android.html
  // and modified by Steve Pomeroy <steve@staticfree.info>
  private final float duration = 0.1f; // seconds
  private final int sampleRate = 8000;
  private final int numSamples = (int) (duration * sampleRate);

  private final byte toneSound[] = new byte[2 * numSamples];
  // private final byte generatedSound[] = new byte[2 * numSamples];

  private AudioTrack audioTrack;

  public TonePlayer() {
    setFrequency(660);
    // setDutyCycle(0.3f);
    createNative();
  }

  /*
   * // / What percentage of time is the tone on void setDutyCycle(float onTime)
   * { // We copy some number of the tone bytes followed by 1-onTime of zero //
   * bytes int splitPoint = 2 * (int) (numSamples * onTime);
   * 
   * for (int i = 0; i < splitPoint; i++) generatedSound[i] = toneSound[i];
   * 
   * for (int j = splitPoint; j < 2 * numSamples; j++) generatedSound[j] = 0; }
   */

  // / @param freqOfTone in Hz
  void setFrequency(float freqOfTone) {
    // fill out the array
    int idx = 0;
    for (int i = 0; i < numSamples; ++i) {
      double dVal = Math.sin(2 * Math.PI * i / (sampleRate / freqOfTone));

      // convert to 16 bit pcm sound array
      // assumes the sample buffer is normalised.

      // scale to maximum amplitude
      final short val = (short) ((dVal * 32767));
      // in 16 bit wav PCM, first byte is the low order byte
      toneSound[idx++] = (byte) (val & 0x00ff);
      toneSound[idx++] = (byte) ((val & 0xff00) >>> 8);
    }
  }

  private void createNative() {
    audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
        AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
        numSamples, AudioTrack.MODE_STATIC);

    // audioTrack.write(generatedSound, 0, generatedSound.length);
    audioTrack.write(toneSound, 0, toneSound.length);

    audioTrack.setNotificationMarkerPosition(numSamples - 1);
  }

  public void play() {
    audioTrack.stop();
    audioTrack.reloadStaticData();
    audioTrack.play();
  }
}