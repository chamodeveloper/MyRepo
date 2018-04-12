// Copyright (c) 2013, Conviva Inc.  All rights reserved.
package com.conviva.api;

import java.util.Map;
import java.util.HashMap;


/**
 * Encapsulates all content metadata for a particular video asset.
 */
public class ContentMetadata {
    /**
     * A unique identifier for the content, preferably human-readable.
     */
    public String assetName = null;

    /**
     * A string-to-string dictionary of custom metadata.
     */
    public Map<String, String> custom;

    /**
     * Bitrate can be set using setBitrateKbps(final int newBitrateKbps) API in {@link com.conviva.api.player.PlayerStateManager}
     */
    @Deprecated
    public int defaultBitrateKbps = -1;

    /**
     * Default video server resource to report for this content.<br>
     * Use when the video server resource cannot be directly inferred from ContentMetadata.streamUrl
     * Examples: EDGECAST, AKAMAI-FREE, LEVEL3-PREMIUM...
     */    
    public String defaultResource = null;

    /**
     * A string identifying the viewer.
     */
    public String viewerId = null;

    /**
     * A string identifying the current application.
     */
    public String applicationName = null;

    /**
     * Required. The URL from where the video content will be downloaded.
     */
    public String streamUrl = null;
    
    /**
	 * The mode of streaming for this content.
     */
	public static enum StreamType {
	    /** It is not yet known what the type of streaming is. */
	    UNKNOWN,
	    /** Content for this session is streamed live. */
	    LIVE,
	    /** Content for this session is streamed on demand. */
	    VOD,
	};

    /**
     * Required. The mode of streaming for this session.
     */
    public StreamType streamType = StreamType.UNKNOWN;
    
    /**
     * Duration of the video content, in seconds
     */
    public int duration = 0;

    /**
     * Encoded frame rate of the video content, in frames per second.
     */
    public int encodedFrameRate = 0;

    /**
     * Encapsulates all content metadata for a particular video asset.
     */
    public ContentMetadata() {
    }

    /**
     * Copy constructor
     * @param contentMetadata including assetName,duration.
     */
    public ContentMetadata(ContentMetadata contentMetadata) {
        if (contentMetadata == null) return;

        this.assetName = contentMetadata.assetName;
        this.defaultBitrateKbps = contentMetadata.defaultBitrateKbps;
        this.defaultResource = contentMetadata.defaultResource;
        this.duration = contentMetadata.duration;
        this.encodedFrameRate = contentMetadata.encodedFrameRate;
        this.streamType = contentMetadata.streamType;
        this.applicationName = contentMetadata.applicationName;
        this.streamUrl = contentMetadata.streamUrl;
        this.viewerId = contentMetadata.viewerId;
        if (contentMetadata.custom != null && !contentMetadata.custom.isEmpty()) {
            this.custom = new HashMap<String, String>(contentMetadata.custom);
        }
    }

}
