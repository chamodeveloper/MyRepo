// Copyright (c) 2013, Conviva Inc.  All rights reserved.
package com.conviva.session;

import java.util.Map;

import com.conviva.api.ContentMetadata;
import com.conviva.session.Monitor.InternalPlayerState;
import com.conviva.internal.StreamerError;

/**
 * Interface exposing streamer events to the Monitor.
 */
public interface IMonitorNotifier {
    /**
     * Signal a change in the playing state.
     * @param newState to be set on a state change event.
     */
    void setPlayerState(InternalPlayerState newState);

    /**
     *  Signal a change in the bitrate.
     * @param bitrateKbps bitrate to be set for the playback.
     */
    void setBitrateKbps(int bitrateKbps);

    /**
     * Signal a change in the video width.
     * @param videoWidth width of the video.
     */
    void setVideoWidth(int videoWidth);

    /**
     * Signal a change in the video height.
     * @param videoHeight height of the video.
     */
    void setVideoHeight(int videoHeight);

    /**
     * Signal a change in the CDN Server IP Address.
     * @param newCDNServerIp Updated CDN server IP.
     */
    void setCDNServerIP(String newCDNServerIp);

    /**
     * Signal a seek start
     * @param seekToPos new position that the seek is trying to get to. The is the targeted play head time.
     */
    void onSeekStart(int seekToPos);

    /**
     * Signal a seek end
     */
    void onSeekEnd();

    /**
     * Seek user button down the position bar
     */
    void onSeekButtonDown();

    /**
     * Seek user button up the position bar
     */
    void onSeekButtonUp();

    /**
     * Signals an update in rendered framerate.
     *
     * @param renderedFps rendered framerate to be reported.
     */
    void onRenderedFramerateUpdate(int renderedFps);

    /**
     * Signal an error.
     * @param e error events.
     */
    void onError(StreamerError e);

    /**
     * Merges another instance of ContentMetadata into this current one for this Session.
     * @param contentMetadata Object to be merged with this instance of ContentMetadata.
     */
    void onContentMetadataUpdate(ContentMetadata contentMetadata);

    /**
     * Signal new metadata.
     * @param metadata to be updated.
     */
    void onMetadata(Map<String, String> metadata);
    
    void release() throws Exception;
}
