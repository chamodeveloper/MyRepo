package com.conviva.api.system;

/**
 * Used by the Conviva library to access system timers.
 */

public interface ITimerInterface {
    /** 
     * Start a periodic timer.
     * Will be called frequently.
     * @param timerAction A function to run periodically.
     * @param intervalMs Interval at which to run the function, in milliseconds.
     * @param actionName A human-readable identifier describing the purpose of the timer.
     * @return systemTimer A timer object that can be used to cancel the timer
     */
    public ICancelTimer createTimer(Runnable timerAction, int intervalMs, String actionName);


    /** 
     * Notification that Conviva no longer needs this TimerInterface.
     */
    public void release();
  }
