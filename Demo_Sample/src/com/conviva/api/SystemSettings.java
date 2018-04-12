package com.conviva.api;
/**
 * Settings for the System.
 */
public class SystemSettings {
    /**
     * The level of log messages to print in the console log.<br>
     * We recommend using log level WARNING during development, and lowering to DEBUG when more information is required to troubleshoot specific issues.
     */
    public LogLevel logLevel = SystemSettings.defaultProductionLogLevel;

    /**
     * Whether to allow or catch runtime exceptions.<br>
     * The default value of false is highly recommended in a production environment.
     * Default value of true is recommended during development for easier troubleshooting.
     */
    public boolean allowUncaughtExceptions = SystemSettings.defaultProductionAllowUncaughtExceptions;

    /**
     * How long the Conviva client will wait for a response when attempting to access device storage before considering that attempt a failure.
     * In seconds.
     * The default value will work for the overwhelming majority of devices.
     */
    public int storageTimeout = SystemSettings.defaultStorageTimeout;

    /**
     * How long the Conviva client will wait for a response when attempting to access device storage before considering that attempt a failure. In seconds.<br>
     * The default value will work for the overwhelming majority of devices.
     */
    public int httpTimeout = SystemSettings.defaultHttpTimeout;
	
	
	/**
	 * Possible log level settings for Conviva components.
	 */
	public static enum LogLevel {
	    /** Will display all log messages. */
		DEBUG, 
	    /** Will only display warning and error log messages. */
		INFO, 
	    /** Will only display error log messages. */
		WARNING, 
	    /** Will display all log messages except debugging messages. */
		ERROR,
	    /** Will not display any log messages. */
		NONE
	}

	//  Enable logging to device console
	public static final LogLevel defaultDevelopmentLogLevel = LogLevel.DEBUG;
	public static final LogLevel defaultProductionLogLevel = LogLevel.ERROR;

	// Whether to try/catch exceptions or not (may interfere with some debuggers)
	public static final boolean defaultDevelopmentAllowUncaughtExceptions = true;
	public static final boolean defaultProductionAllowUncaughtExceptions = false;

	// The maximum amount of time we wait when accessing data from persistent storage.
	public static final int defaultStorageTimeout = 10;

	// The maximum amont of time we wait before giving up / cancelling HTTP requests.
	public static final int defaultHttpTimeout = 10;
	
}
