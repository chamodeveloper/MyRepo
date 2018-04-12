package com.conviva.platforms.android;

import com.conviva.api.AndroidSystemInterfaceFactory;

import android.Manifest;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;

import java.util.List;

/**
 * Network Util class used by {@link AndroidSystemInterfaceFactory} to fetch network related metrics from Devices.
 */
public class AndroidNetworkUtils {


    // Connection Type possible values
    private static final String CONNECTION_TYPE_WIFI = "WiFi";
    private static final String CONNECTION_TYPE_ETHERNET = "Ethernet";
    private static final String CONNECTION_TYPE_OTHER = "OTHER";

    // Security Type possible values
    private static final String SECURITY_WPA2 = "WPA2";
    private static final String SECURITY_WPA = "WPA";
    private static final String SECURITY_EAP = "EAP";
    private static final String SECURITY_WEP = "WEP";
    private static final String SECURITY_NONE = "NONE";

    private static final int DEFAULT_SIGNAL_STRENGTH = 1000;

    private static Context _context = null;
    public static void initWithContext(Context context) {
        if (_context == null ) {
            _context = context;
        }
    }

    /*
     * Internal Use only
     * @return true if the network is available
     */
    public static Boolean isNetworkAvailable() {
        if(_context == null)
            return false;
        if(AndroidSystemUtils.checkPermissionProvided(Manifest.permission.ACCESS_NETWORK_STATE)) {

            ConnectivityManager connectivityManager
                    = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
        }
        return false;
    }

    /*
     * Internal Use only
     * @return true if the wifi is connected.
     */
    public static Boolean isWifiConnected(){
        if(_context == null)
            return false;
        if(isNetworkAvailable()){
            ConnectivityManager cm
                    = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
            return (cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI);
        }
        return false;
    }

    /*
     * Internal Use only
     * @return true if the ethernet is connected.
     */
    public static Boolean isEthernetConnected(){
        if(_context == null)
            return false;
        if(isNetworkAvailable()){
            ConnectivityManager cm
                    = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
            return (cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_ETHERNET);
        }
        return false;
    }

    /*
     * Internal Use only
     * @return the network class type
     */
    public static String getNetworkClass() {
        if(_context == null ) {
            return CONNECTION_TYPE_OTHER;
        }
        TelephonyManager mTelephonyManager = (TelephonyManager)
                _context.getSystemService(Context.TELEPHONY_SERVICE);
        if(mTelephonyManager != null) {
            /* Raw values should be returned to the backend in case
             * of cellular data. Hardcoded strings are returned for
             * Ethernet and Wifi as the raw values will conflict
             */
            return String.valueOf(mTelephonyManager.getNetworkType());
        }
        return CONNECTION_TYPE_OTHER;
    }

    /*
     * Internal Use only
     * @return the network connection type.
     */
    public static String getConnectionType() {
        if (isEthernetConnected()) {
            return CONNECTION_TYPE_ETHERNET;
        } else if (isWifiConnected()) {
            return CONNECTION_TYPE_WIFI;
        } else {
            return getNetworkClass();
        }
    }

    /*
     * Internal Use only
     * @return the SSID of the connected wifi network.
     */
    // SSID code is commented as this is a PII item.
    /*public static String getSSID() {
        if (_context != null && isWifiConnected() && AndroidSystemUtils.checkPermissionProvided(Manifest.permission.ACCESS_WIFI_STATE)) {
            WifiManager wifiManager = (WifiManager) _context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifiManager.getConnectionInfo();
            String ssid = info.getSSID().replace("\"", "");        // SSID returned from Android is with "". e.g. "Wifi-Name"
            return ssid;
        }
        return null;
    }*/

    /*
     * Internal Use only
     * @return the signal strength of the wifi/cellular network.
     */
    public static int getSignalStrength() {
        if(_context == null)
            return DEFAULT_SIGNAL_STRENGTH;
        
        if (isEthernetConnected()) {
            return DEFAULT_SIGNAL_STRENGTH;
        } else if (isWifiConnected()) {
            if(AndroidSystemUtils.checkPermissionProvided(Manifest.permission.ACCESS_WIFI_STATE)) {
                WifiManager wifiManager = (WifiManager) _context.getSystemService(Context.WIFI_SERVICE);
                return wifiManager.getConnectionInfo().getRssi();
            }else{
                return DEFAULT_SIGNAL_STRENGTH;
            }
        } else {
            TelephonyManager telephonyManager = (TelephonyManager) _context.getSystemService(Context.TELEPHONY_SERVICE);

            if(!AndroidSystemUtils.checkPermissionProvided(Manifest.permission.ACCESS_COARSE_LOCATION))
                return DEFAULT_SIGNAL_STRENGTH;         // If permission not granted. Do not proceed to avoid the security exception

            // Checking for build version as the APIs are introduced in Android SDK VERSION 17
            if(Build.VERSION.SDK_INT >= 17) {
                List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
                if(cellInfoList != null && cellInfoList.size() > 0) {
                    for (CellInfo cellInfo : cellInfoList) {
                        if (cellInfo instanceof CellInfoGsm) {
                            CellSignalStrengthGsm gsm = ((CellInfoGsm) cellInfo).getCellSignalStrength();
                            return gsm.getDbm();
                        } else if (cellInfo instanceof CellInfoCdma) {
                            CellSignalStrengthCdma cdma = ((CellInfoCdma) cellInfo).getCellSignalStrength();
                            return cdma.getDbm();
                        } else if (Build.VERSION.SDK_INT >= 18 && cellInfo instanceof CellInfoWcdma) {
                            CellSignalStrengthWcdma wcdma = ((CellInfoWcdma) cellInfo).getCellSignalStrength();
                            return wcdma.getDbm();
                        } else if (cellInfo instanceof CellInfoLte) {
                            CellSignalStrengthLte lte = ((CellInfoLte) cellInfo).getCellSignalStrength();
                            return lte.getDbm();
                        }
                    }
                }
            }
        }

        return DEFAULT_SIGNAL_STRENGTH;
    }

    /*
     * Internal Use only
     * @return the link encryption type.
     */
    public static String getLinkEncryption() {
        if (_context == null || !isWifiConnected() || !AndroidSystemUtils.checkPermissionProvided(Manifest.permission.ACCESS_WIFI_STATE)) {
            return SECURITY_NONE;
        }
        WifiManager wifiManager = (WifiManager) _context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            WifiConfiguration activeConfig = null;
            for (WifiConfiguration config : wifiManager.getConfiguredNetworks()) {
                if (config.status == WifiConfiguration.Status.CURRENT) {
                    activeConfig = config;
                    break;
                }
            }
            if (activeConfig != null) {
                return getSecurity(activeConfig);
            }
        }

        return SECURITY_NONE;
    }

    private static String getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
            if (config.allowedProtocols.get(WifiConfiguration.Protocol.RSN))
                return SECURITY_WPA2;
            else
                return SECURITY_WPA;
        }
        if (config.allowedKeyManagement.get(KeyMgmt.WPA_EAP) || config.allowedKeyManagement.get(KeyMgmt.IEEE8021X)) {
            return SECURITY_EAP;
        }
        return (config.wepKeys.length > 0 && config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
    }

    public static void release() {
        _context = null;
    }

}
