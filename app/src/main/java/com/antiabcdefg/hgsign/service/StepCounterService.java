package com.antiabcdefg.hgsign.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import androidx.annotation.RequiresApi;

import com.antiabcdefg.hgsign.R;
import com.antiabcdefg.hgsign.ui.ShakeActivity;
import com.antiabcdefg.hgsign.ui.SplashActivity;
import com.antiabcdefg.hgsign.utils.HardwarePedometerUtil;
import com.antiabcdefg.hgsign.utils.MyApplication;

public class StepCounterService extends Service implements SensorEventListener {

    // 每天打开软件记录的初始步数，用于带count传感器算法的方式
    public static int FIRST_STEP = 0;    
    // 当天的记步数总数
    public static int CURRENT_STEP = 0;
    private float avg_v = 0;
    private float min_v = 0;
    private float max_v = 0;

    private int acc_count = 0;
    private int up_c = 0;
    private int down_c = 0;
    private long pre_time = 0;

    public static Boolean isOpen = false;

    private SensorManager mSensorManager;
    private WakeLock mWakeLock;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate() {
        super.onCreate();
        isOpen = true;
        notification();
        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);

        if (HardwarePedometerUtil.supportsHardwareStepCounter(this) && !isForce())
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER), SensorManager.SENSOR_DELAY_UI);
        else if (HardwarePedometerUtil.supportsHardwareAccelerometer(this))
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 50 * 1000);

        PowerManager mPowerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "HGSign:Step");
        mWakeLock.acquire(10*60*1000L); //10 minutes
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isOpen = false;
        if (this != null) {
            mSensorManager.unregisterListener(this);
        }
        if (mWakeLock != null) {
            mWakeLock.release();
        }
        reset();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        mSensorManager = null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == null) {
            return;
        }
        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                // Step Counter，要准确很多,读取开机的传感器步数
                if (FIRST_STEP == 0) {
                    FIRST_STEP = (int) event.values[0];
                } else {
                    CURRENT_STEP = Math.abs((int) event.values[0] - FIRST_STEP);
                    updateNotification(CURRENT_STEP);
                }
            } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float v = (float) Math.sqrt(event.values[0] * event.values[0]
                        + event.values[1] * event.values[1] + event.values[2]
                        * event.values[2]);
                avg_check_v(v);
            }
        }
    }

    private void avg_check_v(float v) {
        acc_count++;
        if (acc_count < 64) avg_v = avg_v + (v - avg_v) / acc_count;
        else avg_v = avg_v * 63 / 64 + v / 64;

        if (v > avg_v) {
            up_c++;
            if (up_c == 1) max_v = avg_v;
            else max_v = Math.max(v, max_v);
            if (up_c >= 2) down_c = 0;
        } else {
            down_c++;
            if (down_c == 1) min_v = v;
            else min_v = Math.min(v, min_v);
            if (down_c >= 2) up_c = 0;
        }

        if (up_c == 2 && (max_v - min_v) > 2) {
            long cur_time = System.currentTimeMillis();
            if (cur_time - pre_time > 250) {
                pre_time = cur_time;
                CURRENT_STEP++;
                updateNotification(CURRENT_STEP);
            } else
                up_c = 1;
        }

    }

    private void reset() {
        avg_v = 0;
        acc_count = 0;
        up_c = 0;
        down_c = 0;
    }

    private boolean isForce() {
        SharedPreferences mPerferences = PreferenceManager.getDefaultSharedPreferences(this);
        return mPerferences.getBoolean("check_preference", false);
    }

    public void notification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int notifyID = 001;
        Notification notification = new Notification.Builder(MyApplication.getContext())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("GPS已开启")
                .setTicker("后台已开启")
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentIntent(PendingIntent.getActivity(
                        this, 0, new Intent(this, ShakeActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .build();

        notificationManager.notify(notifyID, notification);
    }

    public void updateNotification(int step) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int notifyID = 001;

        Intent intent = new Intent()
                .setAction(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setComponent(new ComponentName(this, SplashActivity.class))
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        Notification notification = new Notification.Builder(MyApplication.getContext())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("GPS已开启")
                .setContentText("当前步数为" + step)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT))
                .build();

        notificationManager.notify(notifyID, notification);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
