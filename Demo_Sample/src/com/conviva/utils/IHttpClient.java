package com.conviva.utils;

import com.conviva.api.system.ICallbackInterface;

/**
 * This interface was created primarily so that we can enable mocking, 
 * which is required for Scenario tests.
 */
public interface IHttpClient {
	public void request(String httpMethod, String url, String data, String contentType, ICallbackInterface callback);
}
