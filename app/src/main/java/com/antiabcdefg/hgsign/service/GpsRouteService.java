package com.antiabcdefg.hgsign.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.antiabcdefg.hgsign.utils.CommonUtil;
import com.antiabcdefg.hgsign.utils.DBManager;
import com.antiabcdefg.hgsign.utils.MyAMAPLocation;
import com.antiabcdefg.hgsign.utils.MyApplication;

import java.util.ArrayList;

public class GpsRouteService extends Service {

    private final static int MAXCOUNT = 110;// byte
    private final static int MAXDISTANCE = 7;// m

    private MyAMAPLocation locationClient;
    private DBManager dbManager;
    private boolean isFirstRecoder = true;
    private ArrayList<String> infoarr = null;
    private double oldLa = 0, oldLo = 0;


    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        dbManager = new DBManager(MyApplication.getContext());
        infoarr = new ArrayList<>();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(this::startLocation).start();
        return super.onStartCommand(intent, flags, startId);
    }

    public void startLocation() {
        locationClient = new MyAMAPLocation(MyApplication.getContext());
        locationClient.setOnResponseToLocationListener((street, latitude, longitude, speed, bearing, time, satellites, altitude) -> {
            if (latitude != 0 && longitude != 0) {// 过滤定位失败的点
                if (isFirstRecoder) {
                    oldLa = latitude;
                    oldLo = longitude;
                    infoarr.add(latitude + ";" + longitude + ";" + speed + ";" + bearing + ";" + time + ";" + CommonUtil.getCurrentTime() + "|");
                    isFirstRecoder = false;
                } else {
                    if (oldLa != latitude && oldLo != longitude) {// 过滤原地不动的点
                        infoarr.add(latitude + ";" + longitude + ";" + speed + ";" + bearing + ";" + time + ";" + CommonUtil.getCurrentTime() + "|");
                        oldLa = latitude;
                        oldLo = longitude;
                    }
                }
            }
        }

        );
        locationClient.start();
    }

    public ArrayList<String> correctLocations(ArrayList<String> info, float ratio) {
        ArrayList<String> temp = new ArrayList<>();
        for (int i = 0, len = info.size(); i < len; i++) {
            if (i > 0 && i < len - 1) {
                String locationa = temp.get(i - 1);
                String locationb = info.get(i);
                String locationc = info.get(i + 1);

                String[] arr1 = locationa.split(";");
                String[] arr2 = locationb.split(";");
                String[] arr3 = locationc.split(";");

                double a = Math.abs(CommonUtil.distance(Double.parseDouble(arr2[0]), Double.parseDouble(arr2[1]), Double.parseDouble(arr3[0]), Double.parseDouble(arr3[1])));
                double b = Math.abs(CommonUtil.distance(Double.parseDouble(arr1[0]), Double.parseDouble(arr1[1]), Double.parseDouble(arr3[0]), Double.parseDouble(arr3[1])));
                double c = Math.abs(CommonUtil.distance(Double.parseDouble(arr1[0]), Double.parseDouble(arr1[1]), Double.parseDouble(arr2[0]), Double.parseDouble(arr2[1])));
                double angle_b = Math.acos(((a * a) + (c * c) - (b * b)) / (2 * a * c));

                if (angle_b < Math.PI / 6) {
                    temp.add(locationa);
                } else if (angle_b < Math.PI / 3) {
                    String latemp = String.valueOf(((Double.parseDouble(arr1[0]) + Double.parseDouble(arr2[0]) + Double.parseDouble(arr3[0])) / 3));
                    String lotemp = String.valueOf(((Double.parseDouble(arr1[1]) + Double.parseDouble(arr2[1]) + Double.parseDouble(arr3[1])) / 3));
                    temp.add(latemp + ";" + lotemp + ";" + arr2[2] + ";" + arr2[3] + ";" + arr2[4] + ";" + arr2[5] + "|");
                } else if (angle_b < Math.PI * ratio) {
                    String latemp = String.valueOf(((Double.parseDouble(arr1[0]) + Double.parseDouble(arr3[0])) / 2));
                    String lotemp = String.valueOf(((Double.parseDouble(arr1[1]) + Double.parseDouble(arr3[1])) / 2));
                    temp.add(latemp + ";" + lotemp + ";" + arr2[2] + ";" + arr2[3] + ";" + arr2[4] + ";" + arr2[5] + "|");
                } else {
                    temp.add(locationb);
                }
            } else {
                temp.add(info.get(i));
            }
        }
        return temp;
    }

    public ArrayList<String> correcteZShape(ArrayList<String> info) {
        int len = info.size();
        if (len <= 5) {
            return info;
        }
        ArrayList<String> temp = new ArrayList<>();
        for (int i = 0; i < len - 4; i++) {
            if (i > 0) {
                String locationa = temp.get(i - 1);
                String locationb = info.get(i);
                String locationc = info.get(i + 1);
                String locationd = info.get(i + 2);

                String[] arr1 = locationa.split(";");
                String[] arr2 = locationb.split(";");
                String[] arr3 = locationc.split(";");
                String[] arr4 = locationd.split(";");

                double angle_b = 0, angle_c = 0;

                double a = Math.abs(CommonUtil.distance(Double.parseDouble(arr2[0]), Double.parseDouble(arr2[1]), Double.parseDouble(arr3[0]), Double.parseDouble(arr3[1])));
                double b = Math.abs(CommonUtil.distance(Double.parseDouble(arr1[0]), Double.parseDouble(arr1[1]), Double.parseDouble(arr3[0]), Double.parseDouble(arr3[1])));
                double c = Math.abs(CommonUtil.distance(Double.parseDouble(arr1[0]), Double.parseDouble(arr1[1]), Double.parseDouble(arr2[0]), Double.parseDouble(arr2[1])));
                if (a > 0 && c > 0) {
                    angle_b = Math.acos(((a * a) + (c * c) - (b * b)) / (2 * a * c));
                }

                double d = Math.abs(CommonUtil.distance(Double.parseDouble(arr2[0]), Double.parseDouble(arr2[1]), Double.parseDouble(arr3[0]), Double.parseDouble(arr3[1])));
                double e = Math.abs(CommonUtil.distance(Double.parseDouble(arr4[0]), Double.parseDouble(arr4[1]), Double.parseDouble(arr3[0]), Double.parseDouble(arr3[1])));
                double f = Math.abs(CommonUtil.distance(Double.parseDouble(arr2[0]), Double.parseDouble(arr2[1]), Double.parseDouble(arr4[0]), Double.parseDouble(arr4[1])));
                if (d > 0 && e > 0) {
                    angle_c = Math.acos(((d * d) + (e * e) - (f * f)) / (2 * d * e));
                }

                double r = (angle_b + angle_c);
                if (r < Math.PI && r > 0) {

                    i++;
                } else temp.add(locationb);

            } else {
                temp.add(info.get(i));
            }
        }
        temp.add(info.get(len - 4));
        temp.add(info.get(len - 3));
        temp.add(info.get(len - 2));
        temp.add(info.get(len - 1));
        return temp;
    }

    @Override
    public void onDestroy() {
        locationClient.stop();
        StringBuilder info = new StringBuilder();
//        CommonUtil.ToastLong(MyApplication.getContext(), "正在记录，请稍后...");
        for (String i : infoarr) {
            info.append(i);
        }
        if (info.length() >= MAXCOUNT) {
            dbManager.add(info.toString(), CommonUtil.getCurrentDate(), "false");
            CommonUtil.ToastLong(MyApplication.getContext(), "路径已记录，点击左侧边栏查看路径");
        } else {
            infoarr.clear();
            CommonUtil.ToastLong(MyApplication.getContext(), "路径太短，无法记录");
        }
        infoarr.clear();
        dbManager.closeDB();
        super.onDestroy();
    }
}
