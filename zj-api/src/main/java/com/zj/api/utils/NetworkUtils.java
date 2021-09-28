package com.zj.api.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;

@SuppressWarnings("unused")
public class NetworkUtils {

    /**
     * Get network type
     */
    public static NetworkType networkType(Context context) {
        try {
            if (isDeniedPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
                return NetworkType.TYPE_NONE;
            }
            // Wifi
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (manager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Network network = manager.getActiveNetwork();
                    if (network != null) {
                        NetworkCapabilities capabilities = manager.getNetworkCapabilities(network);
                        if (capabilities != null) {
                            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                                return NetworkType.TYPE_WIFI;
                            } else if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) && !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) && !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                                return NetworkType.TYPE_NONE;
                            }
                        }
                    } else {
                        return NetworkType.TYPE_NONE;
                    }
                } else {
                    NetworkInfo networkInfo = manager.getActiveNetworkInfo();
                    if (networkInfo == null || !networkInfo.isConnected()) {
                        return NetworkType.TYPE_NONE;
                    }

                    networkInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
                        return NetworkType.TYPE_WIFI;
                    }
                }
            }
            // Mobile network
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager == null) {
                return NetworkType.TYPE_NONE;
            }
            int networkType = telephonyManager.getNetworkType();
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return NetworkType.TYPE_2G;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return NetworkType.TYPE_3G;
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return NetworkType.TYPE_4G;
                case TelephonyManager.NETWORK_TYPE_NR:
                    return NetworkType.TYPE_5G;
                default:
                    return NetworkType.TYPE_NONE;
            }
        } catch (Exception e) {
            return NetworkType.TYPE_NONE;
        }
    }

    /**
     * Is there an available network
     */
    public static boolean isNetworkAvailable(Context context) {
        if (isDeniedPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
            return false;
        }
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Network network = cm.getActiveNetwork();
                    if (network != null) {
                        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                        if (capabilities != null) {
                            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN);
                        }
                    }
                } else {
                    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                    return networkInfo != null && networkInfo.isConnected();
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isDeniedPermission(Context context, String permission) {
        try {
            Class<?> contextCompat = null;
            try {
                contextCompat = Class.forName("android.support.v4.content.ContextCompat");
            } catch (Exception e) {
                //ignored
            }
            if (contextCompat == null) {
                try {
                    contextCompat = Class.forName("androidx.core.content.ContextCompat");
                } catch (Exception e) {
                    //ignored
                }
            }
            if (contextCompat == null) {
                return true;
            }
            Method checkSelfPermissionMethod = contextCompat.getMethod("checkSelfPermission", Context.class, String.class);
            Object result = checkSelfPermissionMethod.invoke(null, context, permission);
            int r = -1;
            if (result != null) try {
                r = (int) result;
            } catch (Exception ignored) {
            }
            if (r == -1) return true;
            if (r == PackageManager.PERMISSION_GRANTED) {
                Log.i("checkHasPermission", "You can fix this by adding the following to your AndroidManifest.xml file:\n" + "<uses-permission android:name=\"" + permission + "\" />");
                return false;
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    public enum NetworkType {
        TYPE_NONE(0), TYPE_2G(1), TYPE_3G(1 << 1), TYPE_4G(1 << 2), TYPE_WIFI(1 << 3), TYPE_5G(1 << 4), TYPE_ALL(0xFF);

        int type;

        NetworkType(int type) {
            this.type = type;
        }
    }
}
