package com.conviva.platforms.android;

import com.conviva.api.system.ICallbackInterface;
import com.conviva.api.system.IHttpInterface;

import java.net.MalformedURLException;
import java.net.URL;


// Do not subclass AndroidHttpInterface so it can be removed by ProGuard if necessary
/**
 * Conviva provided helper class which implements {@link IHttpInterface}
 * required methods for https requests and drop any non-https requests.
 */
public class AndroidHttpsInterface implements IHttpInterface {

    @Override
    public void makeRequest(String httpMethod, String url, String data,
                            String contentType, int timeoutMs, ICallbackInterface callback) {
        try {
            URL netUrl = new URL(url);
            if (!netUrl.getProtocol().equals("https")) { // drop non-HTTPS requests
                callback.done(false, "plaintext connections not allowed");
                return;
            }
        } catch (MalformedURLException ex) {
            if (callback != null) callback.done(false, ex.toString());
            return;
        }
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
