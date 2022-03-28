package com.antiabcdefg.hgsign.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;

import java.util.Arrays;
import java.util.List;


/**
 * Created by Alost on 16/2/16.
 * 计步器硬件传感器工具类
 */
public class HardwarePedometerUtil {
    public static List<String> mStepCounterWhiteList = Arrays.asList("nexus5", "nexus6", "mx4", "mx5", "mi4");

    private static boolean areSensorsPresent(Context context) {
        return !((SensorManager) context.getSystemService(Context.SENSOR_SERVICE)).getSensorList(Sensor.TYPE_STEP_COUNTER).isEmpty();
    }

    /**
     * step counter白名单列表
     */
    public static boolean isThisDeviceInStepCounterWhiteList() {
        return mStepCounterWhiteList.contains(Build.MODEL.toLowerCase().replace(" ", ""));
    }

    /**
     * 是否支持step counter记步传感器
     * 方式：1、有TYPE_STEP_COUNTER；2、版本为4.4(19)以上
     */
    public static boolean supportsHardwareStepCounter(Context context) {
        SensorManager mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        return (AndroidVersionUtil.isVersionKitKatOrHigher()) && (areSensorsPresent(context));
    }

    /**
     * 是否支持accelerometer传感器
     */
    public static boolean supportsHardwareAccelerometer(Context context) {
        return !((SensorManager) context.getSystemService(Context.SENSOR_SERVICE)).getSensorList(Sensor.TYPE_ACCELEROMETER).isEmpty();
    }

}
