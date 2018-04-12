package com.conviva.api.system;

/**
 * Used by the Conviva library to make HTTP(S) requests.
 */

public interface IHttpInterface {

    /** 
     * Send an HTTP request. HTTPS should be handled as well.
     * Will be called regularly when Conviva monitoring sessions are active.
     * @param httpMethod "POST" or "GET".
     * @param url Where to make the HTTP request to.
     * @param data Data to send along for POST requests.
     * @param contentType Content type to be used in HTTP headers.
     * @param timeoutMs Timeout to apply to the request, in milliseconds. The request must be cancelled after that amount of time has passed.
     * @param callback Callback to call when done.
     */
	public void makeRequest(String httpMethod, String url, String data, String contentType, int timeoutMs, ICallbackInterface callback); 

	/** 
     * Notification that Conviva no longer needs this HttpInterface object.
     */
    public void release();
}
