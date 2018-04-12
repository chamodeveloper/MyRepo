package com.conviva.utils;

import java.util.concurrent.Callable;

import com.conviva.api.system.ICancelTimer;
import com.conviva.api.system.ITimerInterface;

/**
 * Timer
 * Used by the Conviva library to create system timer.
 */

public class Timer {
	private ITimerInterface _timerInterface;
	private ExceptionCatcher _exceptionCatcher;
	private Logger _logger;
	
	public Timer(Logger logger, ITimerInterface timerInterface, ExceptionCatcher exceptionCatcher) {
		_timerInterface = timerInterface;
		_exceptionCatcher = exceptionCatcher;
		_logger = logger;
	}
	
    /** 
     * Creates a recurring timer.
     * @param timerAction The action to be performed
     * @param intervalMs Frequency of timer
     * @param actionName Name of the timer
     * @return Optional. A function that can be called to cancel the timer.
     */

	public ICancelTimer createRecurring(Runnable timerAction, int intervalMs,
			String actionName) {
		ICancelTimer cancelTimer;
		
		class WrappedTimerAction implements Runnable {
			private String _actionName;
			private Runnable _timerAction;
			public WrappedTimerAction(String actionName, Runnable timerAction) {
				_actionName = actionName;
				_timerAction = timerAction;
			}
			
			@Override
			public void run() {
				if (_exceptionCatcher != null) {
					try {
						_exceptionCatcher.runProtected(new Callable<Void>() {
						    public Void call() throws Exception {
						    	_timerAction.run();
						        return null;
						    }
						}, _actionName);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		}
		cancelTimer = this.createTimer(new WrappedTimerAction(actionName, timerAction), intervalMs, actionName);
		return cancelTimer;
	}

    /** 
     * Creates a one shot timer.
     * @param timerAction The action to be performed
     * @param intervalMs Frequency of timer
     * @param actionName Name of the timer
     * @return Optional. A function that can be called to cancel the timer.
     */

	public ICancelTimer createOneShot(Runnable timerAction, int intervalMs,
			String actionName) {
		ICancelTimer cancelTimer = null;
		
		class WrappedTimerAction implements Runnable {
			private String _actionName;
			private Runnable _timerAction;
			private ICancelTimer _cancelTimer;
			private boolean _timerActionHappened;
			
			public WrappedTimerAction(String actionName, Runnable timerAction) {
				_actionName = actionName;
				_timerAction = timerAction;
				_cancelTimer = null;
				_timerActionHappened = false;
			}
			
			public void setCancelTimer(ICancelTimer cancelTimer) {
				_cancelTimer = cancelTimer;
			}
			
			public boolean getTimerActionHappened() {
				return _timerActionHappened;
			}
			
			@Override
			public void run() {
				if (_exceptionCatcher != null) {
					try {
						_exceptionCatcher.runProtected(new Callable<Void>() {
						    public Void call() throws Exception {
						    	if (_cancelTimer != null) {
						    		_cancelTimer.cancel();
						    		_cancelTimer = null;
						    	}
						    	_timerAction.run();
						    	_timerActionHappened = true;
						        return null;
						    }
						}, _actionName);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		WrappedTimerAction wrappedTimerAction = new WrappedTimerAction(actionName, timerAction);
		cancelTimer = this.createTimer(wrappedTimerAction, intervalMs, actionName);
		wrappedTimerAction.setCancelTimer(cancelTimer);
		
        // This is necessary because makeTimer() might have already caused
        // wrappedAction() to be called (e.g. it might call the action
        // synchronously if delayMs=0).  In that case, theTimer was null
        // when wrappedAction was called, so theTimer couldn't be cleaned
        // up.
		
		if (wrappedTimerAction.getTimerActionHappened()) {
	    	if (cancelTimer != null) {
	    		cancelTimer.cancel();
	    		cancelTimer = null;
	    	}
		}
		return cancelTimer;
	}
	
    public ICancelTimer createTimer(Runnable timerAction, int intervalMs, String actionName) {
    	_logger.debug("createTimer(): calling TimerInterface.createTimer");
    	return _timerInterface.createTimer(timerAction, intervalMs, actionName);
    }

}
