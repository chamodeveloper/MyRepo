// Copyright (c) 2013, Conviva Inc.  All rights reserved.
package com.conviva.utils;

import android.util.Log;

/**
 * Class that sends messages for logging.
 */
public class TLog {

    /** Send informational messages.
     *
     * @param tag Log TAG
     * @param msg Log Message
     */
    public static void i(String tag, String msg){
        Log.i(tag, msg);
    }

    /** Send error messages.
     *
     * @param tag Log TAG
     * @param msg Log Message
     */
    public static void e(String tag, String msg){
        Log.e(tag, msg);
    }

    /** Send debug messages.
     *
     * @param tag Log TAG
     * @param msg Log Message
     */
    public static void d(String tag, String msg){
        Log.d(tag, msg);
    }
}
