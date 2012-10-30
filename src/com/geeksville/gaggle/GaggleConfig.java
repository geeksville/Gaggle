package com.geeksville.gaggle;

public class GaggleConfig {
	private static boolean useBarometer = true;

	public static boolean isUseBarometer() {
		return useBarometer;
	}

	public static void setUseBarometer(boolean useBarometer) {
		GaggleConfig.useBarometer = useBarometer;
	}
}
