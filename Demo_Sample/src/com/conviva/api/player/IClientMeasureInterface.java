package com.conviva.api.player;


/**
 * Reports player related metrics required by Conviva SDK.
 */
public interface IClientMeasureInterface {
    /**
     * Return the pht of player instance in milliseconds.
     * @return A long value of the play head time.
     */
    public long getPHT();

    /**
     * Return the buffer length of player instance in milliseconds.
     * @return An integer value of buffer length.
     */

    public int getBufferLength();

    /**
     * Return the signal strength of client.
     * @return A double value of the signal strength.
     */

    public double getSignalStrength();

    /**
     * Return the frame rate of the player.
     * @return A integer value of the frame rate.
     */
    public int getFrameRate();

}
