// Copyright (c) 2013, Conviva Inc.  All rights reserved.
package com.conviva.internal;

import com.conviva.api.Client;

/**
 * Used by Conviva library to store an error's severity, scope, code, etc.
 */
public class StreamerError {

    private String _errorCode;
    private Client.ErrorSeverity _severity;

    /// @brief Create an error instance based on parameters
    public StreamerError(String errorCode, Client.ErrorSeverity severity) {
        _errorCode = errorCode;
        _severity = severity;
    }

    /// Retrieve error code for this error instance
    public String getErrorCode() {
        return _errorCode;
    }

    /// Retrieve error severity for this error instance
    public Client.ErrorSeverity getSeverity() {
        return _severity;
    }
}
