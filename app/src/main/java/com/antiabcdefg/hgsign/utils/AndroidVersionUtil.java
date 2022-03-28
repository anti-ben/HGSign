package com.antiabcdefg.hgsign.utils;

import android.os.Build;

public class AndroidVersionUtil {
    public static boolean isVersion18OrHigher() {
        return Build.VERSION.SDK_INT >= 18;
    }

    public static boolean isVersionJellyBeanMr2OrHigher() {
        return Build.VERSION.SDK_INT >= 18;
    }

    public static boolean isVersionJellyBeanOrHigher() {
        return Build.VERSION.SDK_INT >= 16;
    }

    public static boolean isVersionKitKatOrHigher() {
        return Build.VERSION.SDK_INT >= 19;
    }

    public static boolean isVersionLollipopOrHigher() {
        return Build.VERSION.SDK_INT >= 21;
    }

    public static boolean isVersionMOrHigher() {
        return Build.VERSION.SDK_INT >= 23;
    }
}
