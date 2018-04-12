package com.conviva.protocol;

import java.util.HashMap;
import java.util.Map;

import com.conviva.session.Monitor.InternalPlayerState;
import com.conviva.utils.SystemMetadata;

/**
 * Stores and reports meta-data and player states in accordance with the CWS protocol version.
 */
public class Protocol {
    // default to CWS 1.7 until we enabled in test player or next release.
	public static String version = "2.4";

	public static String  gatewayPath = "/0/wsg"; 
	public static String  DEFAULT_CLIENT_ID = "0";
	public static String BACKEND_RESPONSE_NO_ERRORS = "ok";
	public static String SDK_METADATA_SCHEMA = "sdk.android.1";
	
    /// Dictionary that maps streamer states from String to ints
    /// \note Call init() before using and cleanup() afterwards
    public static Map<String, Integer> stateToInt = null;
	
	// Player States
    public static final int ePlaying = 3;
    public static final int eStopped = 1;
    public static final int ePaused = 12;
    public static final int eBuffering = 6;
    public static final int eNotMonitored = 98;
    public static final int eUnknown = 100;	
	
    /**
     * Types of data reported by the session.
     */
    /** The session will not report any video playback data. */
    public static final int CAPABILITY_GLOBAL = 0;
    /** The session will report video playback data. */
    public static final int CAPABILITY_VIDEO = 1;
    /** The session will report video quality data. */
    public static final int CAPABILITY_QUALITY_METRICS = 2;
    /** The session will report video bitrate data. */
    public static final int CAPABILITY_BITRATE_METRICS = 4;

	public Map<String, String> buildPlatformMetadata(Map<String, String> systemMetadata) {
		Map<String, String> md = new HashMap<String, String>();
		md.put("sch", SDK_METADATA_SCHEMA);
        if (systemMetadata.containsKey(SystemMetadata.ANROID_BUILD_MODEL))
        	md.put("abm", systemMetadata.get(SystemMetadata.ANROID_BUILD_MODEL));
        if (systemMetadata.containsKey(SystemMetadata.OPERATING_SYSTEM_VERSION))
        	md.put("osv", systemMetadata.get(SystemMetadata.OPERATING_SYSTEM_VERSION));
        if (systemMetadata.containsKey(SystemMetadata.DEVICE_BRAND))
        	md.put("dvb", systemMetadata.get(SystemMetadata.DEVICE_BRAND));
        if (systemMetadata.containsKey(SystemMetadata.DEVICE_MANUFACTURER))
        	md.put("dvma", systemMetadata.get(SystemMetadata.DEVICE_MANUFACTURER));
        if (systemMetadata.containsKey(SystemMetadata.DEVICE_MODEL))
        	md.put("dvm", systemMetadata.get(SystemMetadata.DEVICE_MODEL));
        if (systemMetadata.containsKey(SystemMetadata.DEVICE_TYPE))
        	md.put("dvt", systemMetadata.get(SystemMetadata.DEVICE_TYPE));
        if (systemMetadata.containsKey(SystemMetadata.DEVICE_VERSION))
        	md.put("dvv", systemMetadata.get(SystemMetadata.DEVICE_VERSION));
        if (systemMetadata.containsKey(SystemMetadata.FRAMEWORK_NAME))
        	md.put("fw", systemMetadata.get(SystemMetadata.FRAMEWORK_NAME));
        if (systemMetadata.containsKey(SystemMetadata.FRAMEWORK_VERSION))
        	md.put("fwv", systemMetadata.get(SystemMetadata.FRAMEWORK_VERSION));
        return md;
	}
	
	public static int convertPlayerState(InternalPlayerState playerState) {
		if (playerState == InternalPlayerState.STOPPED)
			return Protocol.eStopped;
		if (playerState == InternalPlayerState.PLAYING)
			return Protocol.ePlaying;
		if (playerState == InternalPlayerState.BUFFERING)
			return Protocol.eBuffering;
		if (playerState == InternalPlayerState.PAUSED)
			return Protocol.ePaused;
		if (playerState == InternalPlayerState.NOT_MONITORED)
			return Protocol.eNotMonitored;
		return Protocol.eUnknown;		
	}
}
