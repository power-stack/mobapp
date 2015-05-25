package com.stackbase.mobapp.objects;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tony on 15-5-18.
 */
public class SIMCardInfo {
    private TelephonyManager tMgr;
    private String IMSI;

    private final static String TAG = SIMCardInfo.class.getName();

    public SIMCardInfo(Context context) {
        tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    public long getNativePhoneNumber() {
        long nativePhoneNumber = 0;
        try {
            String phoneNumber = tMgr.getLine1Number();
            nativePhoneNumber = checkPhoneNum(phoneNumber);
        } catch (Exception e) {
            Log.e(TAG, "Fail to get phone number.", e);
        }
        return nativePhoneNumber;
    }

    public String getDeviceID() {
        String model = Build.SERIAL;
        return model;
    }

    public String getProviderName() {
        String providerName = null;
        IMSI = tMgr.getSubscriberId();

        if (IMSI.startsWith("46000") || IMSI.startsWith("46002"))
            providerName = "中国移动";
        else if (IMSI.startsWith("46001"))
            providerName = "中国联通";
        else if (IMSI.startsWith("40003"))
            providerName = "中国电信";

        return providerName;
    }

    // Reg to check phone number and replace 86 or +86
    protected static long checkPhoneNum(String phoneNum) throws Exception {
        Pattern p1 = Pattern.compile("^((\\+{0,1}86){0,1})1[0-9]{10}");
        Matcher m1 = p1.matcher(phoneNum);
        if (m1.matches()) {
            Pattern p2 = Pattern.compile("^((\\+{0,1}86){0,1})");
            Matcher m2 = p2.matcher(phoneNum);
            StringBuffer sb = new StringBuffer();
            while (m2.find()) {
                m2.appendReplacement(sb, "");
            }
            m2.appendTail(sb);
            return Long.parseLong(sb.toString());

        } else {
            throw new Exception("The format of phoneNum " + phoneNum
                    + "  is not correct!Please correct it");
        }
    }
}
