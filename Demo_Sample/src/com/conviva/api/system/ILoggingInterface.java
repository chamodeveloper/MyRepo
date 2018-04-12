package com.conviva.api.system;

import com.conviva.api.SystemSettings.LogLevel;

/**
 * Used by the Conviva library to log messages to debug console.
 */

public interface ILoggingInterface {
    /** 
     * Console logging.
     * Will be called frequently if logging is enabled.
     * @param message The message to be logged to the console.
     * @param logLevel The log level for that message.
     */
    public void consoleLog(String message, LogLevel logLevel);

    /** 
     * Notification that Conviva no longer needs this LoggingInterface.
     */
    public void release();
}
