package com.conviva.utils;

import com.conviva.api.SystemSettings;

/**
 * This interface was created primarily so that we can enable mocking,
 * which is required for Scenario tests.
 */

public interface ILogger {
    /** 
     * Associates a module name with this Logger.
     * @param moduleName The module name to use for this logger.
     */
    public void setModuleName(String moduleName);
    
    /** 
     * Associates a session ID with this Logger.
     * @param sessionId The session ID to use for this logger.
     */
    public void setSessionId(int sessionId);
    
    /** 
     * Logs a message to the console at DEBUG level.
     * @param message The message to log at DEBUG level.
     */
    public void debug(String message);
    
    /** 
     * Logs a message to the console at INFO level.
     * @param message The message to log at INFO level.
     */
    public void info(String message);
    
    /** 
     * Logs a message to the console at WARNING level.
     * @param message The message to log at WARNING level.
     */
    public void warning(String message);

    /** 
     * Logs a message to the console at ERROR level.
     * @param message The message to log at ERROR level.
     */
    public void error(String message);
    
    /*
     * Used only for for debugging when we want to log to console
     * but not log to buffer
     */
    public void consoleLog(String message, SystemSettings.LogLevel logLevel);
}
