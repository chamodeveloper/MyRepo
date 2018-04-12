package com.conviva.platforms.android;

import java.io.File;
import java.util.List;

import com.conviva.api.system.IGraphicalInterface;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.os.Process;

/**
 * Conviva provided helper class which implements {@link IGraphicalInterface} required methods.
 */
public class AndroidGraphicalInterface implements IGraphicalInterface {
	private Context _context = null;

	public AndroidGraphicalInterface(Context context) {
		_context = context;
	}
	
	@Override
	public boolean traceOverride() {
        // If debug file exists, override tracing
        String file = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/conviva_debug.txt";
        File debugFile = new File(file);
        return debugFile.exists();
	}

	@Override
	public boolean inSleepingMode() {
        PowerManager pm = (PowerManager) _context.getSystemService(Context.POWER_SERVICE);
        return !pm.isScreenOn();
	}

	@Override
	public boolean isVisible() {
        int myPid = Process.myPid();
        ActivityManager am = (ActivityManager) _context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procs = am.getRunningAppProcesses();
        if(procs == null) {
            return false;
        }
        for(int i = 0; i < procs.size(); i++) {
            if(procs.get(i).pid == myPid) {
                if(procs.get(i).importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return true;
	}

    public boolean isDataSaverEnabled() {
        ConnectivityManager connMgr = null;
        if (_context != null) {
            connMgr = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }

        if(!AndroidSystemUtils.checkPermissionProvided(Manifest.permission.ACCESS_NETWORK_STATE))
            return false;

        if (connMgr != null && Build.VERSION.SDK_INT >= 24) {
            if (connMgr.isActiveNetworkMetered()) {
                // isActiveNetworkMetered() always returns true for Cellular data & false for WiFi
                switch (connMgr.getRestrictBackgroundStatus()) {
                    case ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED:
                        // Data saver is enabled & Background data usage is blocked for this app.
                        return true;

                    case ConnectivityManager.RESTRICT_BACKGROUND_STATUS_WHITELISTED:
                        // Data saver is enabled & the app is whitelisted.
                        // specific permissions should be added for whitelisting in application
                        return false;

                    case ConnectivityManager.RESTRICT_BACKGROUND_STATUS_DISABLED:
                        // Data saver is disabled while device is on Metered(Cellular) network
                        return false;
                }
            }
        }
        // The device is not on a metered network.
        // Use data as required to perform syncs, downloads, and updates.
        return false;

    }

	@Override
	public void release() {
		// nothing to release
	}

}
