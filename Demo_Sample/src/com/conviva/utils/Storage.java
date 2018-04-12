package com.conviva.utils;

import com.conviva.api.SystemSettings;
import com.conviva.api.system.ICallbackInterface;
import com.conviva.api.system.IStorageInterface;

/**
 * Storage
 * Used by the Conviva library to access local storage.
 */

public class Storage {
	private Logger _logger;
	private IStorageInterface _storageInterface;
	private CallbackWithTimeout _callbackWithTimeout;
	private SystemSettings _systemSettings;
	private final static String STORAGE_SPACE = "Conviva";
	
	public Storage(Logger logger, IStorageInterface storageInterface, CallbackWithTimeout callbackWithTimeout, SystemSettings systemSettings) {
		_logger = logger;
		_storageInterface = storageInterface;
		_callbackWithTimeout = callbackWithTimeout;
		_systemSettings = systemSettings;
	}
	
    public void load(String key, ICallbackInterface callback) {
        ICallbackInterface wrapperCallback = this._callbackWithTimeout.getWrapperCallback(
        		callback,
                this._systemSettings.storageTimeout * 1000,
                "storage load timeout"
            );
    	
        
        _logger.debug("load(): calling StorageInterface.loadData");
        this._storageInterface.loadData(Storage.STORAGE_SPACE, key, wrapperCallback);
    }

    public void save(String key, String data, ICallbackInterface callback) {
        ICallbackInterface wrapperCallback = this._callbackWithTimeout.getWrapperCallback(
        		callback,
                this._systemSettings.storageTimeout * 1000,
                "storage save timeout"
            );
        _logger.debug("load(): calling StorageInterface.saveData");
        this._storageInterface.saveData(Storage.STORAGE_SPACE, key, data, wrapperCallback);
    }

    public void delete(String key, ICallbackInterface callback) {
		this._storageInterface.deleteData(Storage.STORAGE_SPACE, key, callback);
    };
}
