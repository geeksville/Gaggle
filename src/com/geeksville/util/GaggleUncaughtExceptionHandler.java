package com.geeksville.util;

import java.lang.Thread.UncaughtExceptionHandler;

import android.util.Log;

public class GaggleUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
	final private UncaughtExceptionHandler parent;
	
	public GaggleUncaughtExceptionHandler(UncaughtExceptionHandler ueh) {
		this.parent = ueh;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		Log.e("Gaggle", "Uncaught", ex);
		
		if (parent != null) this.parent.uncaughtException(thread, ex);
	}

}
