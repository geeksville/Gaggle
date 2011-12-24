package com.geeksville.util;

/**
 * 
 * @author kevinh From: http://blueflyvario.blogspot.com/2011_05_01_archive.html
 *         A more simple approach is to use an IIR filter. This wikipedia
 *         article will confuse the hell out of most people. It is actually
 *         really easy when explained in words. You measure the altitude the
 *         first time. This becomes the 'current' altitude. With each subsequent
 *         measurement you sum X% of the new measurement with (100 - X)% of the
 *         previous 'current' altitude for the new 'current' altitude. This ends
 *         up being an exponential filter, where the most recent measurements
 *         have more weight than older measurements. The X% is the damping
 *         factor. Around 5 or 10 is about right, the lower the number the more
 *         damping. More damping equals more smoothness but more lag.
 */
public class IIRFilter {
	private float current = Float.NaN;
	private float dampingFactor;

	public IIRFilter(float dampingFactor) {
		this.dampingFactor = dampingFactor;
	}

	public void addSample(float v) {
		if (Float.isNaN(current))
			current = v;
		else
			current = dampingFactor * v + (1 - dampingFactor) * current;
	}

	public float get() {
		return current;
	}
}
