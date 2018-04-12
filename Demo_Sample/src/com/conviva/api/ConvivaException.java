package com.conviva.api;

/**
 * Custom Exception for Conviva Android SDK.
 */
public class ConvivaException extends Exception {

    public ConvivaException() {
        super();
    }

    public ConvivaException(String message) {
        super(message);
    }

    public ConvivaException(String message, Throwable cause) {
        super(message, cause);
    }
}
