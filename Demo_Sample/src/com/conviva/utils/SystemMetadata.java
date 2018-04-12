package com.conviva.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import com.conviva.api.Client;
import com.conviva.api.Client.DeviceType;
import com.conviva.api.system.IMetadataInterface;

/**
 * Class to store system metadata related information.
 */
public class SystemMetadata {
	public static String ANROID_BUILD_MODEL = "androidBuildModel";
	public static String OPERATING_SYSTEM_VERSION = "operatingSystemVersion";
	public static String DEVICE_BRAND = "deviceBrand";
	public static String DEVICE_MANUFACTURER = "deviceManufacturer";
	public static String DEVICE_MODEL = "deviceModel";
	public static String DEVICE_TYPE = "deviceType";	
	public static String DEVICE_VERSION = "deviceVersion";
	public static String FRAMEWORK_NAME = "frameworkName";
	public static String FRAMEWORK_VERSION = "frameworkVersion";
	
	private IMetadataInterface _metadataInterface;
	private ExceptionCatcher _exceptionCatcher;
	private Logger _logger;
	private Map<String, String> _cachedMetadata = null;
	
	public SystemMetadata(Logger logger, IMetadataInterface metadataInterface, ExceptionCatcher exceptionCatcher ) {
		_metadataInterface = metadataInterface;
		_exceptionCatcher = exceptionCatcher;		
		_logger = logger;
	}
	
    /** 
     * Get metadata
     * @throws Exception when getting the metadata.
	 * @return a map object.
     */
    public Map<String, String> get() throws Exception {
        if (_cachedMetadata == null) {
            this.retrieve();
        }
        return this._cachedMetadata;
    }
	
    
    /** 
     * Retrieve metadata
     * @throws Exception when retrieving the metadata.
     */
    public void retrieve() throws Exception {
    	_cachedMetadata = new HashMap<String, String>();

        class MyCallable implements Callable<Void> {
			@Override
			public Void call() throws Exception {
	           _logger.debug("retrieve(): calling MetadataInterface methods");
				if (_metadataInterface.getAndroidBuildModel() != null)
	            	_cachedMetadata.put(ANROID_BUILD_MODEL, _metadataInterface.getAndroidBuildModel());
	            if (_metadataInterface.getOperatingSystemVersion()!= null)
	            	_cachedMetadata.put(OPERATING_SYSTEM_VERSION, _metadataInterface.getOperatingSystemVersion());
	            if (_metadataInterface.getDeviceBrand()!= null)
	            	_cachedMetadata.put(DEVICE_BRAND, _metadataInterface.getDeviceBrand());
	            if (_metadataInterface.getDeviceManufacturer() != null)
	            	_cachedMetadata.put(DEVICE_MANUFACTURER, _metadataInterface.getDeviceManufacturer()); //manufacturer
	            if (_metadataInterface.getDeviceModel() != null)
	            	_cachedMetadata.put(DEVICE_MODEL, _metadataInterface.getDeviceModel()); // model
	            if (_metadataInterface.getDeviceType() != DeviceType.UNKNOWN)
	            	_cachedMetadata.put(DEVICE_TYPE, _metadataInterface.getDeviceType().toString());
	            if (_metadataInterface.getDeviceVersion() != null)
	            	_cachedMetadata.put(DEVICE_VERSION, _metadataInterface.getDeviceVersion());
	            if (_metadataInterface.getFrameworkName() != null)
	            	_cachedMetadata.put(FRAMEWORK_NAME, _metadataInterface.getFrameworkName());
	            if (_metadataInterface.getFrameworkVersion() != null)
	            	_cachedMetadata.put(FRAMEWORK_VERSION, _metadataInterface.getFrameworkVersion());				
				return null;
			}
        }
        
    	_exceptionCatcher.runProtected(new MyCallable(), "SystemMetadata.retrieve");        

        // Validate the enum for deviceType
        if (_cachedMetadata.containsKey(DEVICE_TYPE)) {
        	String deviceType = _cachedMetadata.get(DEVICE_TYPE);
        	
            if (deviceType == Client.DeviceType.UNKNOWN.toString()) {
            		_cachedMetadata.remove(DEVICE_TYPE);
            }
        }
    }
    
}
