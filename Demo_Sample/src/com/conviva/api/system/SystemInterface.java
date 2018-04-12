package com.conviva.api.system;

/**
 * SystemInterface used by the Conviva library to access system information and
 * utilities.
 */

public class SystemInterface {
	private ITimeInterface _timeInterface = null;
	private ITimerInterface _timerInterface = null;
	private IHttpInterface _httpInterface = null;
	private IStorageInterface _storageInterface = null;
	private IMetadataInterface _metadataInterface = null;
	private ILoggingInterface _loggingInterface = null;
	private IGraphicalInterface _graphicalInterface = null;

	/**
	 * Whether the Class successfully initialized or not.
	 */
	private volatile boolean initialized = false;


	/**
	 * Returns true if the client has been initialized and not released.
	 * @return If Client has been succesfully initialized and not released.
	 */
	public boolean isInitialized() {
		return initialized;
	}

	public ITimeInterface getTimeInterface() {
		return this._timeInterface;
	}
	public ITimerInterface getTimerInterface() { return this._timerInterface; }
	public IHttpInterface getHttpInterface() {
		return this._httpInterface;
	}
	public IStorageInterface getStorageInterface() {
		return this._storageInterface;
	}
	public IMetadataInterface getMetadataInterface() {
		return this._metadataInterface;
	}
	public ILoggingInterface getLoggingInterface() {
		return this._loggingInterface;
	}
	public IGraphicalInterface getGraphicalInterface() {
		return this._graphicalInterface;
	}


    /**
     * Constructs SystemInterface
     * Used by the Conviva library to access system information and utilities.
     * @param timeInterface The ITimeInterface to use in this factory.
     * @param timerInterface The ITimerInterface to use in this factory.
     * @param httpInterface The IHttpInterface to use in this factory.
     * @param storageInterface The IStorageInterface to use in this factory.
     * @param metadataInterface The IMetadataInterface to use in this factory.
     * @param loggingInterface The ILoggingInterface to use in this factory.
     * @param graphicalInterface The IGraphicalInterface to use in this factory.
     */
	public SystemInterface(ITimeInterface timeInterface,
							ITimerInterface timerInterface,
							IHttpInterface httpInterface,
							IStorageInterface storageInterface,
							IMetadataInterface metadataInterface,
							ILoggingInterface loggingInterface,
							IGraphicalInterface graphicalInterface) /* throws Exception */{
		
		if (timeInterface == null ||
			timerInterface == null ||
			httpInterface == null ||
			storageInterface == null ||
			metadataInterface == null ||
			loggingInterface == null ||
			graphicalInterface == null) {
			initialized = false;
			return;
		}
		_timeInterface = timeInterface;
		_timerInterface = timerInterface;
		_httpInterface = httpInterface;
		_storageInterface = storageInterface;
		_metadataInterface = metadataInterface;
		_loggingInterface = loggingInterface;
		_graphicalInterface = graphicalInterface;
		initialized = true;
	}
	
    /**
     * Releases resources held by SystemInterface
     */	
	public void release() {

		if (_timeInterface != null) {
			_timeInterface.release();
			_timeInterface = null;
		}
		if (_timerInterface != null) {
			_timerInterface.release();
			_timerInterface = null;
		}
		if (_httpInterface != null) {
			_httpInterface.release();
			_httpInterface = null;
		}
		if (_storageInterface != null) {
			_storageInterface.release();
			_storageInterface = null;
		}
		if (_metadataInterface != null) {
			_metadataInterface.release();
			_metadataInterface = null;
		}
		if (_loggingInterface != null) {
			_loggingInterface.release();
			_loggingInterface = null;
		}
		if (_graphicalInterface != null) {
			_graphicalInterface.release();
			_graphicalInterface = null;
		}
	}
}
