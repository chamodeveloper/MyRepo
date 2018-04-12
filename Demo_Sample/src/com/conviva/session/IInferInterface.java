// Copyright (c) 2013, Conviva Inc.  All rights reserved.
package com.conviva.session;


/**
 * Interface exposing interface PlayerAdapter. Needs to implement if
 * PlayerAdapter wants to use inbuilt Infer logic. This is only for internal
 * Conviva use. SDK users would not have access to this and should have theirs.
 */
public interface IInferInterface {
    /**
     * Return The current position of the play head, in milliseconds since the
     * start of the content.
     *
     * @return The current position of the play head, in milliseconds since the
     *         start of the content. <code>-1</code> if not available.
     */
    public int GetPlayheadTimeMs();
    
    /**
     * Return the buffer length threshold (in milliseconds) at which the
     * streamer will transition from buffering to playing. (Most streamers do
     * not begin playing immediately when data is available, but rather wait for
     * the buffer to reach a certain threshold before playing, ensuring smoother
     * playback). For example, if this number is 1000, and the streamer enters
     * the buffering state with an empty buffer at time 0, the streamer will
     * begin playing once the buffer is sufficient to play 1000ms of media.
     * <p>
     * This threshold can change.
     * <p>
     *
     * @return <code>-1</code> if unknown or unavailable.
     */
    public int GetStartingBufferLengthMs();
    
    /**
     * Return the buffer length threshold (in milliseconds) at which the
     * streamer will transition from playing to buffering. For example, if this
     * number is 1000, the streamer will begin buffering once the buffer has
     * less than 1000ms of media.
     * <p>
     * This threshold can change.
     * <p>
     * @return <code>-1</code> if unknown or unavailable.
     */
    public int GetMinBufferLengthMs();
    
    /**
     * Return The number of milliseconds-worth of data present in the buffer.
     * <p>
     *
     * @return The number of milliseconds-worth of data present in the buffer.
     *         <code>-1</code> if not available.
     */
    public int GetBufferLengthMs();
}
