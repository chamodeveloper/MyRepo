package com.conviva.utils;

import com.conviva.api.system.ICallbackInterface;

/**
 * CallbackWithTimeout is Used by the Conviva library to create system timer.
 * Only handles callbacks with the signature callback(isSuccess, data/errorMsg).
 */

public class CallbackWithTimeout {
	private Timer _timer;
	
	public CallbackWithTimeout(Timer timer) {
		_timer = timer;
	}
	
	public ICallbackInterface getWrapperCallback(ICallbackInterface callback, int timeoutMs, String timeoutMessage) {
		
		class WrapperCallback implements ICallbackInterface, Runnable {
			private ICallbackInterface _callback;
			private int _timeoutMs;
			private String _timeoutMessage;
			private boolean _calledBack;
			
			public WrapperCallback(ICallbackInterface callback, int timeoutMs, String timeoutMessage) {
				_callback = callback;
				_timeoutMs = timeoutMs;
				_timeoutMessage = timeoutMessage;
				_calledBack = false;
			}
						
			@Override
			public void done(boolean succeeded, String data) {
				if (!_calledBack) {
					_calledBack = true;
					_callback.done(succeeded, data);
				}
			}

			@Override
			public void run() {
				if (!_calledBack) {
					_calledBack = true;
					_callback.done(false, _timeoutMessage + " (" + _timeoutMs + " ms)");
				}
			}
		}
		
		WrapperCallback wrapperCallback = new WrapperCallback(callback, timeoutMs, timeoutMessage);
		_timer.createOneShot(wrapperCallback, timeoutMs, "CallbackWithTimeout.wrap");
		
		return wrapperCallback;
	}
}
