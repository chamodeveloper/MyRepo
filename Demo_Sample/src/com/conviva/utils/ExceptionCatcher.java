package com.conviva.utils;

import java.util.concurrent.Callable;

import com.conviva.api.ConvivaException;
import com.conviva.api.SystemSettings;

/**
 * ExceptionCatcher
 * Captures exceptions, logs errors and sends pings.
 */

public class ExceptionCatcher {
	private Logger _logger;
	private Ping _ping;
	private SystemSettings _systemSettings;
	
	public ExceptionCatcher(Logger logger, Ping ping, SystemSettings systemSettings) {
	    this._logger = logger;
	    this._logger.setModuleName("ExceptionCatcher");
	    this._ping = ping;
	    this._systemSettings = systemSettings;
	}
	
    public <V> void runProtected(Callable<V> func, String message) throws ConvivaException {
        try {
            func.call();
        } catch (Exception e) {
        	if (this._systemSettings.allowUncaughtExceptions) {
                // rethrow with exception chain
        		throw new ConvivaException("Conviva Internal Failure " + message, e);
        	} else {
                onUncaughtException(message, e);
        	}
        }
    }

    private void onUncaughtException(String msg, Exception e) {
        try {
            this._ping.send("Uncaught exception: " + msg+ ": " + e.toString());
        } catch (Exception eping) {
            this._logger.error("Caught exception while sending ping: " + eping.toString());
        }
    }
}
