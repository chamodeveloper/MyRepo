
package com.conviva.api.system;


/**
 * System timer object that can be used to cancel the timer.
 */
public interface ICancelTimer {

    /** 
     * Cancel the timer
     * @return true if successful
     */
	public boolean cancel();
}
