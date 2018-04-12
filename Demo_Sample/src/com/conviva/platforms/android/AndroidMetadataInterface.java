
package com.conviva.platforms.android;

import com.conviva.api.Client;
import com.conviva.api.Client.DeviceType;
import com.conviva.api.system.IMetadataInterface;
import com.conviva.api.system.SystemInterface;

import android.app.UiModeManager;
import android.content.res.Configuration;
import android.os.Build;
import android.content.Context;

/**
 * Conviva provided helper class which implements {@link IMetadataInterface}
 * required methods. The application can implement its own
 * {@link IMetadataInterface} conforming class for creating
 * {@link SystemInterface} while creating a {@link Client}.
 */
public class AndroidMetadataInterface implements IMetadataInterface {

	private Context _context = null;

	public AndroidMetadataInterface(Context context) {
		_context = context;
	}
	
	@Override
	public String getAndroidBuildModel() {
		return Build.MODEL;
	}

	@Override
	public DeviceType getDeviceType() {

        // UI_MODE_TYPE_TELEVISION added in API 13
		if (Build.VERSION.SDK_INT >= 13 && _context != null) {
			UiModeManager uiModeManager = (UiModeManager) _context.getSystemService(Context.UI_MODE_SERVICE);
			if (uiModeManager != null && uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
                return DeviceType.SETTOP;
            }
		}
		
		return DeviceType.UNKNOWN;
	}

	@Override
	public String getDeviceVersion() {
		return null;
	}

	@Override
	public String getDeviceModel() {
		return null;
	}

	@Override
	public String getDeviceManufacturer() {
		return Build.MANUFACTURER;
	}

	@Override
	public String getDeviceBrand() {
		return Build.BRAND;
	}

	@Override
	public String getOperatingSystemVersion() {
		return Build.VERSION.RELEASE;
	}

	@Override
	public String getFrameworkName() {
		return null;
	}

	@Override
	public String getFrameworkVersion() {
		return null;
	}

	@Override
	public void release() {
		// nothing to release
	}

}
