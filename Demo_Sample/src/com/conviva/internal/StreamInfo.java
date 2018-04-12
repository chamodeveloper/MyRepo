// Copyright (c) 2013, Conviva Inc.  All rights reserved.
package com.conviva.internal;

/**
 * A stream to which a ConvivaStreamerProxy can potentially switch. This object
 * contains no information about the actual URL of the stream it represents.
 */
public class StreamInfo {

    /// The bitrate of the stream, in kilo-bits-per-second. Pass -1 if the
    /// bitrate of this stream is unknown or irrelevant.
    private int _bitrateKbps = -1;

    // The name of the resource from which this stream is loaded. For example,
    // this could be the name of a CDN.  Use null if the resource of this
    // stream is unknown or irrelevant
    private String _resource = null;

    // The name of the CDN on which the resource exists.
    // Use null if the CDN is unknown
    private String _cdnName = null;

    /**
     * 
     * @param bitrateKbps
     *            The bitrate of the stream, in kilo-bits-per-second, or 1000
     *            bits per second. Pass -1 if the bitrate of this stream is
     *            unknown or irrelevant.
     * @param cdnName
     *            The name of the CDN from which the resource is loaded. Pass
     *            null if the CDN is unknown
     * @param resource
     *            The name of the resource from which this stream is loaded.
     *            Pass null if the resource of this stream is unknown or
     *            irrelevant.
     */
    public StreamInfo(int bitrateKbps, String cdnName, String resource) {
        _bitrateKbps = bitrateKbps;
        _cdnName = cdnName;
        /*
        if(_cdnName == null) {
            _cdnName = ConvivaContentInfo.CDN_NAME_OTHER;
        }
        */
        _resource = resource;
    }

    /**
     * @return bitrate of the stream, in 1000 bits per second; -1 if unknown
     */
    public int getBitrateKbps() {
        return _bitrateKbps;
    }

    /**
     *  Sets the bitrate
     * @param bitrateKbps bitrate of the stream
     */
    public void setBitrateKbps(int bitrateKbps) {
    	_bitrateKbps = bitrateKbps;
    }
    
    /**
     * Returns the CDN name
     * 
     * @return CDN name.
     */
    public String getCdnName() {
        return _cdnName;
    }
    
    /**
     *  Sets the CDN name
     * @param cdnName CDN name
     */
    public void setCdnName(String cdnName) {
    	_cdnName = cdnName;
    }

    /**
     *  The resource from which the stream is loaded. null if unknown
     * @return the name of the resource from which this stream is loaded.
     */
    public String getResource() {
        return _resource;
    }

    /**
     *  Set the resource
     * @param resource The name of the resource from which this stream is loaded.
     */
    public void setResource(String resource) {
    	_resource = resource;
    }
    
    /**
     * Returns true if resource, cdnName and bitrate of both StreamInfo instances match
     * @param other StreamInfo instance to be compared
     * @return true if resource, cdnName and bitrate of both StreamInfo instances match else false.
     */
    public boolean equals(StreamInfo other) {
        return _bitrateKbps == other.getBitrateKbps() &&
            _resource == other.getResource() &&
            _cdnName == other.getCdnName();
    }
}
