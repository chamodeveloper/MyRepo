package com.conviva.api.system;

import com.conviva.api.Client.DeviceType;

/**
 * Used by the Conviva library to gather system information.
 * Methods will be called when first calling Client.createSession for each application cycle.
 */

public interface IMetadataInterface {
    /** 
     * Required. The build model of Android
     * @return deviceType Android Build Model
     */
	public String getAndroidBuildModel();

	/** 
     * Required. Type of the device. One of Console, Settop, Mobile, PC
     * @return deviceType Type of the device.
     */
    public DeviceType getDeviceType();
    
    /** 
     * A version of the device. Usually low-level information pertaining to the hardware.
     * Ex: "DTP-BP-0869-34", 
     * @return deviceVersion Version of the device.
     */
    public String getDeviceVersion();

    /** 
     * Model of the device.
     * @return deviceModel Model of the device.
     */
    public String getDeviceModel();
    
    /** 
     * Manufacturer of the device.
     * Ex: "Samsung", "Apple"
     * @return deviceManufacturer Manufacturer of the device.
     */
    public String getDeviceManufacturer();

    /** 
     * Brand of the device.
     * Ex: "iPhone", "Samsung SmartTV" 
     * @return deviceBrand Brand of the device.
     */
    public String getDeviceBrand();


    /** 
     * A version of the operating system used by the device.
     * Ex: "4.2.2"
     * @return operatingSystemVersion Version of the operating system used by the device.
     */
    public String getOperatingSystemVersion();

    /** 
     * Name of the framework used by your application, if applicable.
     * It can be the name of application framework itself, or the name of the player framework used for video playback.<br>
     * Ex: "AVFoundation", "NexPlayer", "HTML5", "OSMF"
     * @return frameworkName Name of the framework used by your application.
     */
    public String getFrameworkName();

    /**
     * A version of the framework used by your application, if applicable.
     * Ex: "alpha12", "4.28.4433"
     * @return frameworkVersion Version of the framework used by your application.
     */
    public String getFrameworkVersion();

        
    /** 
     * Notification that Conviva no longer needs this MetadataInterface.
     */
    public void release();
}
