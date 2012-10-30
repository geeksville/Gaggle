package com.geeksville.gaggle;

/**
 * A class to support testing - e.g. by disabling the barometer for testing GPS altitude calculations.
 * 
 * @author chanoch
 *
 */
public class GaggleConfig {
	/**
	 * Value which determines whether to use the barometer when available.
	 * 
	 * Set to false in order to test your device without using the barometer even when the device has
	 * one.
	 * 
	 * This won't make your device use a barometer if there isn't one on it.
	 */
	private static boolean useBarometer = true;

	/**
	 * Use the built in barometer (if available?)
	 * 
	 * @return
	 */
	public static boolean isUseBarometer() {
		return useBarometer;
	}

	/**
	 * Change the value of the use barometer when available config programatically
	 * 
	 * @param useBarometer
	 */
	public static void setUseBarometer(boolean useBarometer) {
		GaggleConfig.useBarometer = useBarometer;
	}
}
