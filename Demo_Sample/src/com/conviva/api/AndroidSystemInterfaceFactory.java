package com.conviva.api;

import com.conviva.api.system.SystemInterface;
import com.conviva.platforms.android.AndroidGraphicalInterface;
import com.conviva.platforms.android.AndroidHttpInterface;
import com.conviva.platforms.android.AndroidHttpsInterface;
import com.conviva.platforms.android.AndroidLoggingInterface;
import com.conviva.platforms.android.AndroidMetadataInterface;
import com.conviva.platforms.android.AndroidStorageInterface;
import com.conviva.platforms.android.AndroidSystemUtils;
import com.conviva.platforms.android.AndroidTimeInterface;
import com.conviva.platforms.android.AndroidTimerInterface;
import com.conviva.platforms.android.AndroidNetworkUtils;

import android.content.Context;

/**
 * Default AndroidSystemInterfaceFactory provided by Conviva.
 * You can use this to get an instance of SystemInterface.
 * Alternatively you can develop your own SystemInterface.
 */
public class AndroidSystemInterfaceFactory {
	/**
	 * Builds a System Interface factory for Android with {@link AndroidHttpInterface}.
	 * @param context Android Context
	 * @return SystemInterface instance for Android
	 */
	public static SystemInterface build(Context context) {
		// TODO refactoring into System Interface
        AndroidNetworkUtils.initWithContext(context);
		AndroidSystemUtils.initWithContext(context);
		return 	new SystemInterface(new AndroidTimeInterface(), 
									new AndroidTimerInterface(), 
									new AndroidHttpInterface(), 
									new AndroidStorageInterface(context), 
									new AndroidMetadataInterface(context), 
									new AndroidLoggingInterface(), 
									new AndroidGraphicalInterface(context));
	}

        /**
         * Builds a System Interface factory for Android with {@link AndroidHttpInterface}.
         * @param context Android Context
         * @return SystemInterface instance for Android
         */
	public static SystemInterface buildSecure(Context context) {
		AndroidNetworkUtils.initWithContext(context);
		AndroidSystemUtils.initWithContext(context);
		return 	new SystemInterface(new AndroidTimeInterface(),
				new AndroidTimerInterface(),
				new AndroidHttpsInterface(),
				new AndroidStorageInterface(context),
				new AndroidMetadataInterface(context),
				new AndroidLoggingInterface(),
				new AndroidGraphicalInterface(context));
	}
}
