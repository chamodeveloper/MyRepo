package com.conviva.api;

import java.util.LinkedList;
import java.util.List;


import com.conviva.platforms.android.AndroidNetworkUtils;
import com.conviva.protocol.Protocol;
import com.conviva.session.SessionFactory;
import com.conviva.utils.CallbackWithTimeout;
import com.conviva.utils.ExceptionCatcher;
import com.conviva.utils.HttpClient;
import com.conviva.utils.Logger;
import com.conviva.utils.Ping;
import com.conviva.utils.Storage;
import com.conviva.utils.SystemMetadata;
import com.conviva.utils.Time;
import com.conviva.utils.Timer;
import com.conviva.utils.Config;
import com.conviva.api.Client;
import com.conviva.api.system.*;
import com.conviva.json.IJsonInterface;
import com.conviva.json.SimpleJsonInterface;

/**
 * Provides access to system information and utilities according to chosen settings.
 */
public class SystemFactory {
	private SystemInterface _systemInterface;
	private ITimeInterface _timeInterface;
	private ITimerInterface _timerInterface;
	private IHttpInterface _httpInterface;
	private IStorageInterface _storageInterface;
	private IMetadataInterface _metadataInterface;
	private ILoggingInterface _loggingInterface;
	private IGraphicalInterface _graphicalInterface;
	private SystemSettings _settings;
	private String _packageName = null;
    private List<String> _logBuffer = new LinkedList<String>();
    private ClientSettings _clientSettings;
    
    /**
     * Constructs SystemFactory
     * See tutorial 3-integrating-with-the-video-application
     * Provides access to system information and utilities according to chosen settings.
     * @param systemInterface The SystemInterface to use for this factory.
     * @param systemSettings The SystemSettings to use for this factory.
     */
    public SystemFactory(SystemInterface systemInterface, SystemSettings systemSettings) {
        this._systemInterface = systemInterface;

        this._timeInterface = this._systemInterface.getTimeInterface();
        this._timerInterface = this._systemInterface.getTimerInterface();
        this._httpInterface = this._systemInterface.getHttpInterface();
        this._storageInterface = this._systemInterface.getStorageInterface();
        this._metadataInterface = this._systemInterface.getMetadataInterface();
        this._loggingInterface = this._systemInterface.getLoggingInterface();
        this._graphicalInterface = this._systemInterface.getGraphicalInterface();
        
        if (systemSettings == null)
        	systemSettings = new SystemSettings();
        
        this._settings = systemSettings; 
    }

    /*
     * Internal: Do not use
     * @param name Internal: Do not use.
     * @param clientSettings Internal: Do not use.
     */
    public void configure(String name, ClientSettings clientSettings) {
        this._packageName = name;
        this._clientSettings = clientSettings;
    }

    /** 
     * Unloads this SystemFactory.
     */    
    public void release() {
        this._systemInterface.release();
        this._systemInterface = null;

        this._packageName = null;
        this._settings = null;

        this._logBuffer = null;
        AndroidNetworkUtils.release();
    };

    /*
     * Internal: Do not use
     * @return Internal: Do not use.
     */
    public Logger buildLogger() {
        return new Logger(_loggingInterface, _timeInterface, getSettings(), _logBuffer, _packageName);
    }

    /*
     * Internal: Do not use
     * @param client Internal: Do not use.
     * @param clientSettings Internal: Do not use.
     * @param clientConfig Internal: Do not use.
     * @return Internal: Do not use.
     */
    public SessionFactory buildSessionFactory(Client client, ClientSettings clientSettings, Config clientConfig) {
        return new SessionFactory(client, clientSettings, clientConfig, this);
    }

    /*
     * Internal: Do not use
     * @return Internal: Do not use.
     */
    public Ping buildPing() {
        return new Ping(this.buildLogger(), this.buildHttpClient(), _clientSettings); 
    }

    /*
     * Internal: Do not use
     * @return Internal: Do not use.
     */
    public CallbackWithTimeout buildCallbackWithTimeout() {
        return new CallbackWithTimeout(this.buildTimer());
    }

    /*
     * Internal: Do not use
     * @return Internal: Do not use.
     */
    public HttpClient buildHttpClient() {
        return new HttpClient(this.buildLogger(), this._httpInterface, this.getSettings()); //, this.buildTimer());
    }

    /*
     * Internal: Do not use
     * @return Internal: Do not use.
     */
    public ExceptionCatcher buildExceptionCatcher() {
        return new ExceptionCatcher(this.buildLogger(), this.buildPing(), this.getSettings());
    }

    /*
     * Internal: Do not use
     * @return Internal: Do not use.
     */
    public Time buildTime() {
        return new Time(this._timeInterface);
    }

    /*
     * Internal: Do not use
     * @return Internal: Do not use.
     */
    public Timer buildTimer() {
        return new Timer(this.buildLogger(), this._timerInterface, this.buildExceptionCatcher());
    }

    /*
     * Internal: Do not use
     * @return Internal: Do not use.
     */
    public Storage buildStorage() {
        return new Storage(this.buildLogger(), this._storageInterface, this.buildCallbackWithTimeout(), this.getSettings());
    }

    /*
     * Internal: Do not use
     * @return Internal: Do not use.
     * @param client Internal: Do not use.
     */
    public Config buildConfig(Client client) {
        return new Config(this.buildLogger(), this.buildStorage(), this.buildJsonInterface());
    }

    /*
     * Internal: Do not use
     * @return Internal: Do not use.
     */
    public SystemMetadata buildSystemMetadata() {
        return new SystemMetadata(this.buildLogger(), this._metadataInterface, this.buildExceptionCatcher());
    }

    /*
     * Internal: Do not use
     * @return Internal: Do not use.
     */
    public Protocol buildProtocol() {
        return new Protocol();
    }
    
    /*
     * Internal: Do not use
     * @return Internal: Do not use.
     */
    public IGraphicalInterface buildGraphicalInterface() {
        // #TODO: good enough for now
        return this._graphicalInterface;
    }
    
    // #TODO: doesn't belong?
    /*
     * Internal: Do not use
     * @return Internal: Do not use.
     */
    public IJsonInterface buildJsonInterface() {
        return new SimpleJsonInterface();
    }

    /*
     * Internal: Do not use
     * @return Internal: Do not use.
     */
    public List<String> getLogBuffer() {
    	LinkedList<String> cloneLogBuffer = (LinkedList<String>) ((LinkedList<String>)this._logBuffer).clone();
    	this._logBuffer.clear();
        return cloneLogBuffer;
    };

    /*
     * Internal: Do not use
     * @return Internal: Do not use.
     */
    public SystemSettings getSettings() {
        return this._settings;
    }

}
