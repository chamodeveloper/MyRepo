package com.conviva.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import com.conviva.api.SystemSettings;
import com.conviva.api.system.ICallbackInterface;
import com.conviva.json.IJsonInterface;
import com.conviva.protocol.Protocol;
import com.conviva.utils.CallableWithParameters.With0;

/**
 * Config
 * Used by the Conviva library to store and share configuration from local storage.
 */

public class Config {
	private final String CONFIG_STORAGE_KEY_CLIENT_ID = "clId";
	private final String STORAGE_KEY = "sdkConfig";
	private Logger _logger;
	private Storage _storage; 
	private IJsonInterface _jsonInterface;
	private Map<String, Object> _defaultConfig;
	private boolean _loaded;
	private Stack<With0> _waitingConsumers;
	private boolean _loadedEmpty;
	
	public Map<String, Object> _config;
	
	public Config(Logger logger, Storage storage, IJsonInterface json) {
		_logger = logger;
		_storage = storage;
		_jsonInterface = json;
		_loaded = false;
		_waitingConsumers = new Stack<With0>();
		
		_logger.setModuleName("Config");
		
        _defaultConfig = new HashMap<String, Object>();
        _defaultConfig.put("clientId", Protocol.DEFAULT_CLIENT_ID);
        _defaultConfig.put("sendLogs", false); // Not persisted
        
        _config = new HashMap<String, Object>();
        _config.putAll(_defaultConfig);
	}
	
	public boolean isReady() {
		return _loaded;
	}
	
    public void load() {
        
        class LoadedData implements ICallbackInterface {

			@Override
			public void done(boolean success, String data) {
	            if (success) {
	            	if (data != null) {
		                parse(data); // ## should be safe?
		                _logger.debug("load(): configuration successfully loaded from local storage" + 
		                		(_loadedEmpty ? " (was empty)" : "") + ".");
	            	}
	            } else {
	                _logger.error("load(): error loading configuration from local storage: " + data);
	            }
	            _loaded = true;
	            notifyConsumers();
			}
        }
        
		_loadedEmpty = false;
        LoadedData loadedData = new LoadedData();
        
        this._storage.load(STORAGE_KEY, loadedData);
    }

    public void parse(String loadedData) {
    	String loadedClientId = null;
    	Map<String, Object> decodedData = this._jsonInterface.decode(loadedData);
    	
        // If we already received a new valid clientId from backend, don't use the old one.
        // Can't use public setters, they don't work until loaded==true. Change that?
    	if (decodedData == null) {
    		_loadedEmpty = true;
    		return;
    	}
    	
        if (decodedData != null && decodedData.containsKey(CONFIG_STORAGE_KEY_CLIENT_ID)) {
            loadedClientId = decodedData.get(CONFIG_STORAGE_KEY_CLIENT_ID).toString();
        }
        if(loadedClientId != null &&
                !loadedClientId.equals(Protocol.DEFAULT_CLIENT_ID) &&
                !loadedClientId.equals("null") &&
                loadedClientId.length() > 0) {
        	_config.put("clientId", loadedClientId);
        	this._logger.info("parse(): setting the client id to " + loadedClientId + " (from local storage)");
        } else {
        	//this._logger.error("Failed to load the client id from local storage");
        }
    }

    public String marshall() {
        Map<String, Object> configToSave = new HashMap<String, Object>();
        configToSave.put(CONFIG_STORAGE_KEY_CLIENT_ID, this._config.get("clientId"));
        
        return this._jsonInterface.encode(configToSave);
    }

    public void save() {        

    	class SavedData implements ICallbackInterface {
			@Override
			public void done(boolean success, String data) {
	            if (success) {
	                _logger.debug("save(): configuration successfully saved to local storage.");
	            } else {
	                _logger.error("save(): error saving configuration to local storage: " + data);
	            }
			}
        }
        
        SavedData savedData = new SavedData();
        
        this._storage.save(STORAGE_KEY, this.marshall(), savedData);
    }

    public void register(CallableWithParameters.With0 callback) {
        // If we already have the config, call the callback immediately
        if (this.isReady()) {
            callback.exec();
            return;
        }
        // Otherwise remember who asked for the config and notify them when loading is done.
        this._waitingConsumers.push(callback);
    }

    public Object get(String key) {
        if (this._loaded) {
            return this._config.get(key);
        }
        return null;
    }

    public void set(String key, Object value) {
        if (this._loaded) {
            this._config.put(key, value);
        }
    }

    private void notifyConsumers() {
    	CallableWithParameters.With0 callback;
    	if (this._waitingConsumers.empty())
    		return;
        while ((callback = this._waitingConsumers.pop()) != null) {
            // "threading"
            callback.exec();
        }
    }
}
