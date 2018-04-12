package com.conviva.platforms.android;

import com.conviva.api.system.ICallbackInterface;
import com.conviva.api.system.IHttpInterface;

/**
 * Conviva provided helper class which implements {@link IHttpInterface} required methods.
 */
public class AndroidHttpInterface implements IHttpInterface {

	@Override
	public void makeRequest(String httpMethod, String url, String data,
			String contentType, int timeoutMs, ICallbackInterface callback) {
        HTTPTask httpTask = new HTTPTask();
        httpTask.setState(httpMethod, url, data, contentType, timeoutMs, callback);
        Thread thread = new Thread(httpTask);
        if (thread != null) {
            /// @todo: unnecessary thread creation and cleanup overhead per hb
            thread.start();
        }
	}

	@Override
	public void release() {
		// Nothing to release
	}
}
