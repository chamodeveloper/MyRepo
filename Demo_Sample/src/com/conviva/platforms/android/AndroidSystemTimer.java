package com.conviva.platforms.android;

import java.util.concurrent.ScheduledFuture;

import com.conviva.api.system.ICancelTimer;

/**
 * Conviva provided helper class which implements {@link ICancelTimer} required
 * methods.
 */
public class AndroidSystemTimer implements ICancelTimer {
    private ScheduledFuture<?> _scheduledTask = null;
    public AndroidSystemTimer(ScheduledFuture<?> scheduledTask) {
    	_scheduledTask = scheduledTask;
    }
	@Override
	public boolean cancel() {
		_scheduledTask.cancel(true);
		return true;
	}
}
