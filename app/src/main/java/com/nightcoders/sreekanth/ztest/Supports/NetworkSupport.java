package com.nightcoders.sreekanth.ztest.Supports;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import java.util.ArrayList;

import static com.nightcoders.sreekanth.ztest.Literals.Network.CONNECTION_INDEX;
import static com.nightcoders.sreekanth.ztest.Literals.Network.NETWORK_TYPE;
import static com.nightcoders.sreekanth.ztest.Literals.Network.PROVIDER_INDEX;

public class NetworkSupport {

    @SuppressLint("SetTextI18n")
    public static String[] getNetworkProvider(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        assert mTelephonyManager != null;
        String carrierName = mTelephonyManager.getNetworkOperatorName();
        int networkType = mTelephonyManager.getNetworkType();
        String[] val = {"Unknown", carrierName};
        val[1] = carrierName;
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                val[0] = "2G";
                break;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                val[0] = "3G";
                break;
            case TelephonyManager.NETWORK_TYPE_LTE:
                val[0] = "4G";
                break;
            default:
                val[0] = "Unknown";
        }
        return val;
    }

    public ArrayList<String> getNetworkDetails(Context context) {
        ArrayList<String> arrayList = null;
        arrayList.clear();
        if (isConnected(context))
            arrayList.add(CONNECTION_INDEX, "Connected");
        else arrayList.add(CONNECTION_INDEX, "Not Connected");

        TelephonyManager mTelephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        assert mTelephonyManager != null;
        String carrierName = mTelephonyManager.getNetworkOperatorName();
        int networkType = mTelephonyManager.getNetworkType();
        arrayList.add(PROVIDER_INDEX, carrierName);
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
                arrayList.add(NETWORK_TYPE, "GPRS");
                break;
            case TelephonyManager.NETWORK_TYPE_EDGE:
                arrayList.add(NETWORK_TYPE, "EDGE");
                break;
            case TelephonyManager.NETWORK_TYPE_CDMA:
                arrayList.add(NETWORK_TYPE, "CDMA");
                break;
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                arrayList.add(NETWORK_TYPE, "1xRTT");
                break;
            case TelephonyManager.NETWORK_TYPE_IDEN:
                arrayList.add(NETWORK_TYPE, "IDEN");
                break;
            case TelephonyManager.NETWORK_TYPE_UMTS:
                arrayList.add(NETWORK_TYPE, "UMTS");
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                arrayList.add(NETWORK_TYPE, "EVDO_O");
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                arrayList.add(NETWORK_TYPE, "EVDO_A");
                break;
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                arrayList.add(NETWORK_TYPE, "HSDPA");
                break;
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                arrayList.add(NETWORK_TYPE, "HSUPA");
                break;
            case TelephonyManager.NETWORK_TYPE_HSPA:
                arrayList.add(NETWORK_TYPE, "HSPA");
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                arrayList.add(NETWORK_TYPE, "EVDO_B");
                break;
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                arrayList.add(NETWORK_TYPE, "EHRPD");
                break;
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                arrayList.add(NETWORK_TYPE, "HSPAP");
                break;
            case TelephonyManager.NETWORK_TYPE_LTE:
                arrayList.add(NETWORK_TYPE, "LTE");
                break;
            default:
                arrayList.add(NETWORK_TYPE, "Unknown");
                break;
        }
        return arrayList;
    }

    public static boolean isConnected(Context context) {
        final ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert conMgr != null;
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}
