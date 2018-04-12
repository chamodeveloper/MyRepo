package com.conviva.platforms.android;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Looper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * AndroidSystemUtils is used to set Context to Conviva Library.
 */
public class AndroidSystemUtils {

    private static String defaultUserAgent = "UNKNOWN";
    private static Context _context = null;

    // Commenting ADID related code.
    /*public static enum ATI_STATUS {
        ADID_ENABLED(1),
        ADID_DISABLED(0),
        ADID_API_NOT_SUPPORTED(-1);

        private final int val;

        ATI_STATUS(final int value) {
            val = value;
        }

        public int getValue() {
            return val;
        }

    }*/

    public static void initWithContext(Context context) {
        defaultUserAgent = System.getProperty("http.agent");
        if (_context == null ) {
            _context = context;
        }
    }

    public static String getDefaultUserAgent() {
        return defaultUserAgent;
    }

    public static boolean checkPermissionProvided(String permission)
    {
        //Null check added for DE-1022
        if(_context == null){
            return false;
        }
        int res = _context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public static boolean checkMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    // Commenting ADID related code.
    /*public static Object getAdvertisingIdClientInfoObject() {
        if(_context == null) {
            return null;
        }

        try {
            Class<?> _advertisingIdClientClass = Class.forName("com.google.android.gms.ads.identifier.AdvertisingIdClient");
            Method _advertisingIdInfoMethod = _advertisingIdClientClass.getMethod("getAdvertisingIdInfo", Context.class);

            Object _advertisingIdClientInfoObj = _advertisingIdInfoMethod.invoke(null, _context);
            return _advertisingIdClientInfoObj;
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        }

        return null;
    }*/


    /**
     * This API returns "atistatus" to send the status of ati whether enabled or disabled by user.
     * This will be integer value. -1(if platform dont support), 0(adid disabled by user), 1 (adid enabled by user)
     * return
     */
    // Commenting ADID related code.
    /*public static int getATIStatus() {
        Object _advertisingIdClientInfoObj = getAdvertisingIdClientInfoObject();

        if(_advertisingIdClientInfoObj == null )
            return ATI_STATUS.ADID_API_NOT_SUPPORTED.getValue();

        try {
            Method _limitAdTrackingEnabledMethod = _advertisingIdClientInfoObj.getClass().getMethod("isLimitAdTrackingEnabled", null);
            boolean isLimitAdTrackingEnabled = ((Boolean)_limitAdTrackingEnabledMethod.invoke(_advertisingIdClientInfoObj, null)).booleanValue();

            int atiStatus = isLimitAdTrackingEnabled?ATI_STATUS.ADID_DISABLED.getValue():ATI_STATUS.ADID_ENABLED.getValue();

            return atiStatus;
        } catch (NoSuchMethodException e) {
        } catch (InvocationTargetException e) {
        } catch (IllegalAccessException e) {
        }

        return ATI_STATUS.ADID_API_NOT_SUPPORTED.getValue();
    }*/

    // Commenting ADID related code.
    /* public static String getAdID() {
        Object _advertisingIdClientInfoObj = getAdvertisingIdClientInfoObject();

        if(_advertisingIdClientInfoObj == null)
            return null;

        try {
            Method getId = _advertisingIdClientInfoObj.getClass().getMethod("getId", null);
            String adID = (String) getId.invoke(_advertisingIdClientInfoObj, null);
            return adID;

        } catch (NoSuchMethodException e) {
        } catch (IllegalAccessException e) {
        }catch(Exception e) {
        }
        return null;
    }*/

    public static void release() {
        _context = null;
    }
}


