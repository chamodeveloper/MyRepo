package com.conviva.api.system;

/**
 * Used by the Conviva library to query system time.
 */

public interface ITimeInterface {

    /** 
     * Get the current time in milliseconds since Unix epoch.
     * Will be called frequently.
     * @return timeSinceEpochMs Current time in milliseconds since Unix epoch.
     */
	public double getEpochTimeMs();

    /** 
     * Notification that Conviva no longer needs this TimeInterface.
     */
    public void release();
}
