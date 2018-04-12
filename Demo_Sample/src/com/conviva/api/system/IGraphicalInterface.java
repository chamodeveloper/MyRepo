package com.conviva.api.system;

/**
 * Used by the Conviva library to gather misc info.
 */

public interface IGraphicalInterface {
    /*
     * Is the device screen on or off?
     * @return true: screen is off.
     */
    public boolean inSleepingMode();
    
    /*
     * Is the current process visible on screen?
     * @return true: if visble
     */
    public boolean isVisible();    

    /*
     * If log settings set by the application needs to be overridden
     * @return true: override trace settings
     */
    public boolean traceOverride();

    /**
     * Returns if the currently active data network is metered and Background data usage is blocked.
     * @return true : if background data is blocked in metered network.
     */
    public boolean isDataSaverEnabled();

	/** 
     * Notification that Conviva no longer needs this IGraphicalInterface object.
     */
    public void release();    
}
