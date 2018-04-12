package com.conviva.utils;

import com.conviva.api.SystemSettings;
import com.conviva.api.system.ICallbackInterface;
import com.conviva.api.system.IHttpInterface;

/**
 * HttpClient is used by the Conviva library to send HTTP requests.
 */
public class HttpClient implements IHttpClient {
	private IHttpInterface _httpInterface;
	private SystemSettings _systemSettings;
	private Logger _logger;
	
	public HttpClient(Logger logger, IHttpInterface httpInterface, SystemSettings systemSettings) {
		_logger = logger;
		_httpInterface = httpInterface;
		_systemSettings = systemSettings;
	}
	
	public void request(String httpMethod, String url, String data, String contentType, ICallbackInterface callback) {
		_logger.debug("request(): calling IHttpInterface:makeRequest");
		_httpInterface.makeRequest(httpMethod, url, data, contentType, this._systemSettings.httpTimeout * 1000, callback);
	}

}
