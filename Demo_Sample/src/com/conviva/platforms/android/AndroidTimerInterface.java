package com.conviva.platforms.android;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.conviva.api.Client;
import com.conviva.api.system.ICancelTimer;
import com.conviva.api.system.ITimerInterface;
import com.conviva.api.system.SystemInterface;

/**
 * Conviva provided helper class which implements {@link ITimerInterface} required
 * methods. The application can implement its own {@link ITimerInterface} conforming
 * class for creating {@link SystemInterface} while creating a {@link Client}.
 */
public class AndroidTimerInterface implements ITimerInterface {
    // Task pool
    private ScheduledThreadPoolExecutor _pool = null;

    public AndroidTimerInterface() {
        // Use a pool of 2 threads
        _pool = new ScheduledThreadPoolExecutor(2);
    }
    
	@Override
	public ICancelTimer createTimer(Runnable timerAction, int intervalMs,
			String actionName) {
	    ScheduledFuture<?> scheduledTask = null;
        scheduledTask = _pool.scheduleAtFixedRate(timerAction, intervalMs,
                intervalMs, TimeUnit.MILLISECONDS);
		return new AndroidSystemTimer(scheduledTask);
	}

	@Override
	public void release() {
		// Nothing to release
	}

}
