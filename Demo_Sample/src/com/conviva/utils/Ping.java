package com.conviva.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.conviva.api.Client;
import com.conviva.api.ClientSettings;
import com.conviva.protocol.Protocol;

/**
 * Ping
 * Used by the Conviva library to report runtime errors to the Conviva platform.
 */

public class Ping {
	private ILogger _logger;
	private IHttpClient _httpClient;
    private boolean _isSendingPing;
    private boolean _cachedPingUrl;
    private String _basePingUrl;
    private ClientSettings _clientSettings;
    
    private final static String componentName = "sdkjava";
    public static String serviceUrl = "https://pings.conviva.com/ping.ping";

	public Ping(ILogger logger, IHttpClient httpClient, ClientSettings clientSettings) {
		this._isSendingPing = false;
		this._cachedPingUrl = false;
		this._basePingUrl = null;
		this._logger = logger;
	    this._logger.setModuleName("Ping");
		this._httpClient = httpClient;
	    this._clientSettings = clientSettings; 
		
	}

	public void send(String errorMessage) {
        if (this._isSendingPing) {
            // If an error occurs in the process of sending a ping, don't try to send a ping for it.
            // Should not happen unless runProtected methods are used in ping/initPing methods.
            return;
        }
        try {
            this._isSendingPing = true;
            this.init();
            String pingUrl = this._basePingUrl + "&d=" + urlEncodeString(errorMessage);
            this._logger.error("send(): " + pingUrl);
            this._httpClient.request("GET", pingUrl, null, null, null);
            this._isSendingPing = false;
        } catch(Exception e) {
        	_isSendingPing = false;
        	this._logger.error("failed to send ping");
        }    
	}

    private String urlEncodeString(String rawString)
            throws UnsupportedEncodingException {
        return URLEncoder.encode(rawString, "UTF-8");
    }

    public void init() {
        if (!this._cachedPingUrl) { // Prepare the ping URL.
        	this._basePingUrl = Ping.serviceUrl + "?" +
        						"comp=" + Ping.componentName +
        						"&clv=" + Client.version;
        			
            // #TODO: verify whether order matters. if not potentially we can rewrite this.
            if (this._clientSettings != null) {
                _basePingUrl += "&cid=" + this._clientSettings.customerKey;
            }

            // #TODO: not that relevant considering the custom componentName already?
            _basePingUrl += "&sch=" + Protocol.SDK_METADATA_SCHEMA;

            if (this._clientSettings != null) {
            	// All the data is available, we can reuse the same url for later pings.
            	// Otherwise we will recompute it using the latest data.
                this._cachedPingUrl = true;
            }
        }
    }
}
