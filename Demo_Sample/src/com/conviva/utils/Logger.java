package com.conviva.utils;

import java.util.List;

import com.conviva.api.SystemSettings;
import com.conviva.api.system.ILoggingInterface;
import com.conviva.api.system.ITimeInterface;

/**
 * Conviva provided helper class which implements {@link ILogger} required
 * methods.
 */
public class Logger implements ILogger {
    ILoggingInterface _consoleInterface;
    ITimeInterface _timeInterface;
    SystemSettings _settings;
    List<String> _logBuffer;
    String _packageName;
    String _moduleName;
    int _sessionId;

    public Logger(ILoggingInterface consoleInterface,
                  ITimeInterface timeInterface,
                  SystemSettings settings,
                  List<String> logBuffer,
                  String packageName) {
        _consoleInterface = consoleInterface;
        _timeInterface = timeInterface;
        _settings = settings;
        _logBuffer = logBuffer;
        _packageName = packageName;
    }

    /**
     * Logs a message to the console at DEBUG level.
     * @param message The message to log at DEBUG level.
     */
    public void debug(String message) {
        this.log(message, SystemSettings.LogLevel.DEBUG);
    }

    /**
     * Logs a message to the console at INFO level.
     * @param message The message to log at INFO level.
     */
    public void info(String message) {
        this.log(message, SystemSettings.LogLevel.INFO);
    }

    /**
     * Logs a message to the console at WARNING level.
     * @param message The message to log at WARNING level.
     */
    public void warning(String message) {
        this.log(message, SystemSettings.LogLevel.WARNING);
    }

    /**
     * Logs a message to the console at ERROR level.
     * @param message The message to log at ERROR level.
     */
    public void error(String message) {
        this.log(message, SystemSettings.LogLevel.ERROR);
    }

    /**
     * Logs a message to the console through the ConsoleInterface.
     * @param message The message to log.
     * @param logLevel The log level.
     */
    public void log(String message, SystemSettings.LogLevel logLevel) {
        boolean doLog = false;
        String formattedMessage = this.formatMessage(message, logLevel);
        this._logBuffer.add(formattedMessage);
        switch(logLevel) {
            case DEBUG:
                if (this._settings.logLevel == SystemSettings.LogLevel.DEBUG)
                {
                    doLog = true;
                }
                break;

            case INFO:
                if (this._settings.logLevel == SystemSettings.LogLevel.DEBUG ||
                        this._settings.logLevel == SystemSettings.LogLevel.INFO	)
                {
                    doLog = true;
                }
                break;

            case WARNING:
                if (this._settings.logLevel == SystemSettings.LogLevel.DEBUG ||
                        this._settings.logLevel == SystemSettings.LogLevel.INFO	||
                        this._settings.logLevel == SystemSettings.LogLevel.WARNING) {
                    doLog = true;
                }
                break;

            case ERROR:
                if (this._settings.logLevel == SystemSettings.LogLevel.DEBUG ||
                        this._settings.logLevel == SystemSettings.LogLevel.INFO	||
                        this._settings.logLevel == SystemSettings.LogLevel.WARNING ||
                        this._settings.logLevel == SystemSettings.LogLevel.ERROR) {
                    doLog = true;
                }
                break;
        }
        if (doLog)
            this._consoleInterface.consoleLog(formattedMessage, logLevel);
    }

    /**
     * Prepends the name of the module to the message.
     * @param message The original message.
     * @return the module name with the message.
     */
    private String prependModuleName(String message) {
        String tmp = message;
        if (this._moduleName != null && !this._moduleName.isEmpty()) {
            tmp = "["+this._moduleName+"] "+tmp;
        }
        return tmp;
    }

    /**
     * Prepends the name of the package to the message.
     * @param message The original message.
     * @return the packageName with the message.
     */
    public String prependPackageName(String message) {
        String tmp = message;
        if (this._packageName != null && !this._packageName.isEmpty()) {
            tmp = "["+this._packageName+"] "+tmp;
        }
        return tmp;
    };

    /**
     * Prepends Conviva namespace to the message.
     * @param message The original message.
     */
    private String prependConvivaNamespace(String message) {
        String tmp = message;
        if (this._packageName != null && !this._packageName.isEmpty()) {
            tmp = "[Conviva] "+tmp;
        }
        return tmp;
    };

    /**
     * Prepends the name of the module to the message.
     * @param message The original message.
     */
    private String prependTime(String message) {
        double timeMsec = this._timeInterface.getEpochTimeMs();
        String theTime = String.format("%.2f", (timeMsec / 1000.0));
        return "["+theTime+"] "+message;
    };

    /**
     * Prepends the log level to the message.
     * @param {string} message The original message.
     * @param {SystemSettings.LogLevel} logLevel The log level.
     */
    private String prependLogLevel(String message, SystemSettings.LogLevel logLevel) {
        String logLevelString = Logger.getLogLevelString(logLevel);
        String tmp = message;
        /* istanbul ignore else */
        if (this._packageName != null && !this._packageName.isEmpty()) {
            tmp = "["+logLevelString+"] "+tmp;
        }
        return tmp;
    };


    /**
     * Prepends the ID of the current session to the message.
     * @param message The original message.
     */
    private String prependSessionId(String message) {
        String tmp = message;
        if (this._sessionId > 0) {
            tmp = "sid=" + this._sessionId + " " + tmp;
        }
        return tmp;
    };

    /**
     * Associates a session ID with this Logger.
     * @param sessionId The session ID to use for this logger.
     */
    public void setSessionId(int sessionId) {
        this._sessionId = sessionId;
    };

    /**
     * Associates a module name with this Logger.
     * @param moduleName The module name to use for this logger.
     */
    public void setModuleName(String moduleName) {
        this._moduleName = moduleName;
    };

    /**
     * Formats a message for write to the console.
     * @param message The original message.
     */
    private String formatMessage(String message, SystemSettings.LogLevel logLevel) {
        return this.prependConvivaNamespace(this.prependTime(
                this.prependLogLevel(this.prependPackageName(this.prependModuleName(this.prependSessionId(message))), logLevel)));
    }

    @Override
    public void consoleLog(String message, SystemSettings.LogLevel logLevel) {
        String formattedMessage = this.formatMessage(message, logLevel);
        this._consoleInterface.consoleLog(formattedMessage, logLevel);
    }

    /**
     * Maps each log level to a displayable string.
     * @param {SystemSettings.LogLevel} logLevel The log level.
     */
    private static String getLogLevelString(SystemSettings.LogLevel logLevel) {
        String logLevelString = "";
        switch (logLevel) {
            case ERROR:
                logLevelString = "ERROR";
                break;
            case WARNING:
                logLevelString = "WARNING";
                break;
            case INFO:
                logLevelString = "INFO";
                break;
            case DEBUG:
                logLevelString = "DEBUG";
            case NONE:
                logLevelString = "NONE";
            default:
                break;
        }
        return logLevelString;
    }
}
