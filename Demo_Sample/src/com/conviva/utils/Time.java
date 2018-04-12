package com.conviva.utils;

import com.conviva.api.system.ITimeInterface;

/**
 * Time
 * Used by the Conviva library to access system time.
 */

public class Time {
	private ITimeInterface _timeInterface;
	
	public Time(ITimeInterface timeInterface) {
		_timeInterface = timeInterface;
	}
	
    /** 
     * Returns the current time in millseconds since epoch time.
     * @return the current time in millseconds since epoch time.
     */
    public double current() {
        return this._timeInterface.getEpochTimeMs();
    };
	
}
