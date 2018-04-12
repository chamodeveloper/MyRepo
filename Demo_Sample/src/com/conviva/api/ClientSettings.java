package com.conviva.api;

import android.util.Log;

import com.conviva.utils.Lang;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Settings for the client instance. This class handles setting for client
 * required parameters Customer Key, Heartbeat Interval and Gateway URL. The
 * instance of this class is created while creating an instance of {@link Client}.
 */

public class ClientSettings {
    /**
     * Required. Identifies the Conviva account data will be reported to.
     */
    public String customerKey = null;

    /**
     * The time interval at which the Conviva will send available data to the Conviva platform. In seconds.
     * The default value is highly recommended in production environments.
     */
    public int heartbeatInterval = ClientSettings.defaultProductionHeartbeatInterval;

    /**
     * The URL of the Conviva platform to report data to.
     * The default value is highly recommended in production environments.
     */
    public String gatewayUrl = ClientSettings.defaultProductionGatewayUrl;

    /**
     * ClientSettings constructor
     * See tutorial 3-integrating-with-the-video-application
     * @param customerKey The Conviva customer key for the Conviva account data should be transferred to.
     */
    public ClientSettings(String customerKey) {
        if ((customerKey == null) || (customerKey.isEmpty())) {
            Log.e("CONVIVA : ","SDK NOT ready due to lack of customerKey");
            return;
        }
        this.customerKey = customerKey;
    }

    /**
     * ClientSettings constructor
     * Creates a copy of an existing ClientSettings instance.
     * @param clientSettings The instance of ClientSettings to copy.
     */
    public ClientSettings(ClientSettings clientSettings) {
        this(clientSettings.customerKey);
        this.gatewayUrl = clientSettings.gatewayUrl;
        this.heartbeatInterval = clientSettings.heartbeatInterval;
        this.sanitize();
    }

    public boolean isInitialized() {
        return this.customerKey != null;
    }

    private void sanitize() {
    	int hbInterval = this.heartbeatInterval;
    	this.heartbeatInterval = ClientSettings.defaultProductionHeartbeatInterval;
        int sanitizedHeartbeatInterval = Lang.NumberToUnsignedInt(hbInterval);
        if (sanitizedHeartbeatInterval == hbInterval) { // only if the intented value was valid
            this.heartbeatInterval = sanitizedHeartbeatInterval;
        }

        String gwUrl = this.gatewayUrl;
        this.gatewayUrl = "https://" + this.customerKey + ".cws.conviva.com";   // Changing default to https://<customerKey>.cws.conviva.com instead
                                                                                        // of defaultProductionGatewayUrl
        if (Lang.isValidString(gwUrl)) {
            try {
                if(!((new URL(defaultProductionGatewayUrl).getHost()).equals(new URL(gwUrl).getHost())))
                    this.gatewayUrl = gwUrl;
            } catch (MalformedURLException e) {

            }
        }
    }
    
    // Gateway to send data to
    public static final String defaultProductionGatewayUrl = "https://cws.conviva.com";

    // The interval between heartbeats.
    public static final int defaultProductionHeartbeatInterval = 20;

}
