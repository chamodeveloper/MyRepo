package com.conviva.utils;

/**
 * Conviva util class to validate integer and String.
 */
public class Lang {
    public static int NumberToUnsignedInt(int n) {
        // TODO is this valid range for unsigned int?
        return Math.abs(n);
    }

    public static boolean isValidString(String s) {
        if (s != null && !s.isEmpty()) {
            return true;
        }
        return false;
    }
}
