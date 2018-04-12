package com.conviva.platforms.android;

import android.util.Log;

import com.conviva.api.SystemSettings.LogLevel;
import com.conviva.api.system.ILoggingInterface;
import com.conviva.api.system.SystemInterface;

/**
 * Conviva provided helper class which implements {@link ILoggingInterface}
 * The application can implement its own ILoggingInterface conforming class for creating {@link SystemInterface} while creating a client.
 */
public class AndroidLoggingInterface implements ILoggingInterface {
    protected final String _TAG = "CONVIVA";

	@Override
	public void consoleLog(String message, LogLevel logLevel) {
		if (logLevel == LogLevel.DEBUG)
			Log.d(_TAG, message);
		else if (logLevel == LogLevel.ERROR)
			Log.e(_TAG, message);
		else if (logLevel == LogLevel.INFO)
			Log.i(_TAG, message);
		else if (logLevel == LogLevel.WARNING)
			Log.w(_TAG, message);
	}

	@Override
	public void release() {
		// nothing to release
	}

}
