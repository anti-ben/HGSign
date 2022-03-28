package com.antiabcdefg.hgsign.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.antiabcdefg.hgsign.bean.UserBean;
import com.antiabcdefg.hgsign.bean.UserEntity;
import com.antiabcdefg.hgsign.bean.WifiBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class CommonUtil {

    private static Toast toastLong;
    private static Toast toastShort;

    public static void ToastLong(Context context, String message) {
        if (toastLong == null) toastLong = Toast.makeText(context, message, Toast.LENGTH_LONG);
        else {
            toastLong.setText(message);
        }
        toastLong.show();
    }

    public static void ToastShort(Context context, String message) {
        if (toastShort == null) toastShort = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        else {
            toastShort.setText(message);
        }
        toastShort.show();
    }

    public static ProgressDialog getProcessDialog(Context context, String tips) {
        ProgressDialog dialog = null;
        if (dialog == null) {
            dialog = new ProgressDialog(context);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置进度条风格，风格为圆形，旋转的
            dialog.setMessage(tips);
            dialog.setIndeterminate(false); // 设置ProgressDialog 的进度条是否不明确
            dialog.setCancelable(false);// 设置ProgressDialog 是否可以按退回按键取消
            // mypDialog.setIcon(R.drawable.android);//设置ProgressDialog 标题图标
            // mypDialog.setButton("Google",this);//设置ProgressDialog 的一个Button
        }
        return dialog;
    }

    public static void setUser(MyApplication myApplication, UserEntity userEntity) {
        UserBean userBean = new UserBean();
        userBean.setName(userEntity.getUserInfo().getName());
        userBean.setNumber(userEntity.getUserInfo().getUserid());
        userBean.setGetTime(userEntity.getUserInfo().getDate() + " " + userEntity.getUserInfo().getTime());
        userBean.setTempTime(new Date().getTime());
        myApplication.setUserBean(userBean);
    }


    public static ArrayList<WifiBean> getWifiInfo(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        openWifi(context);
        List<ScanResult> wifiList = wifiManager.getScanResults();
        ArrayList<WifiBean> list = new ArrayList<>();
        for (ScanResult scanResult : wifiList) {
            WifiBean wifiBean = new WifiBean();
            wifiBean.setWifiName(scanResult.SSID);
            wifiBean.setWifiMac(scanResult.BSSID);
            if (scanResult.level > -70) {
                list.add(wifiBean);
            }
        }
        return list;
    }

    public static ArrayList<String> getWifiName(Context context) {
        ArrayList<WifiBean> infos = getWifiInfo(context);
        ArrayList<String> names = new ArrayList<>();
        for (WifiBean wifiBean : infos) {
            names.add(wifiBean.getWifiName());
        }
        return names;
    }

    public static void openWifi(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled() && wifiManager != null) {
            ToastLong(MyApplication.getContext(), "打开WIFI中...");
            wifiManager.setWifiEnabled(true);
        }
    }

    public static boolean isConnected(Context context) {
        NetworkInfo info = getActiveNetworkInfo(context);
        return info != null && info.isConnected();
    }

    private static NetworkInfo getActiveNetworkInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    public static String getDeviceID(Context context) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        // String imsi = manager.getSubscriberId();
        String imei = manager.getDeviceId();
        if (imei != null && imei.length() != 0) {
            return imei;
        }
        return "";
    }

    public static String getValidName(String s) {
        if (s.length() > 2 && s.charAt(0) == '\"'
                && s.charAt(s.length() - 1) == '\"') {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    public static boolean isGpsOPen(Context context) {
        LocationManager alm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return alm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static boolean checkPermission(Context context) {
        PackageManager pm = context.getPackageManager();
        return PackageManager.PERMISSION_GRANTED == pm.checkPermission("android.permission.ACCESS_FINE_LOCATION", "com.museum.hgsign");
    }

    public static boolean isValidWLName(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo info = wifiManager.getConnectionInfo();
            if (info != null) {
                String net = getValidName(info.getSSID());
                return isXYW(net);
            } else {
                ToastShort(context, "无法读取到wifi信息");
            }
        } else {
            ToastShort(context, "无法读取到wifi信息");
        }
        return false;
    }

    public static String getValidate(Context context) {
        StringBuilder sb = new StringBuilder();
        ArrayList<WifiBean> wifiBeens = getWifiInfo(context);
        for (WifiBean wifiBean : wifiBeens) {
            if (isXYW(wifiBean.getWifiName())) {
                if (sb.length() == 0) {
                    sb.append(wifiBean.getWifiMac());
                } else {
                    sb.append("|").append(wifiBean.getWifiMac());
                }
            }
        }
        return sb.toString();
    }

    public static Boolean isXYW(String net) {
        return (net.equals("ChinaUnicom") || net.equals("CMCC-EDU")
                || net.equals("ChinaNet-EDU") || net.equals("HYIT") || net
                .equals("@FeiYoung"));
    }

    public static void closeWifi(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            if (isValidWLName(context)) {
                wifiManager.setWifiEnabled(false);
                ToastLong(context, "请保证数据连接处于打开状态！");
            }
        }
    }

    public static boolean isServiceRunning(Context context, String serviceName) {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static double distance(double oldLa, double oldLo, double La, double Lo) {
        double a, b, R, distance, sa2, sb2;
        R = 6378137; // 地球半径
        double lat1 = oldLa * Math.PI / 180.0;
        double lat2 = La * Math.PI / 180.0;
        a = lat1 - lat2;
        b = (oldLo - Lo) * Math.PI / 180.0;
        sa2 = Math.sin(a / 2.0);
        sb2 = Math.sin(b / 2.0);
        distance = 2 * R * Math.asin(Math.sqrt(sa2 * sa2 + Math.cos(lat1) * Math.cos(lat2) * sb2 * sb2));
        return distance;
    }

    public static Date String2Date(String str) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        Date date = null;
        try {
            date = sdf.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String Date2String(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        return sdf.format(date);
    }

    public static boolean istoday(Date target) {
        Date today = new Date();
        if (target != null && today != null) {
            return Date2String(target).equals(Date2String(today));
        }
        return false;
    }

    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        Date nowDate = new Date();
        return sdf.format(nowDate);
    }

    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
        Date nowTime = new Date();
        return sdf.format(nowTime);
    }

    public static String todayBefore(String date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(String2Date(date));
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);
        return Date2String(cal.getTime());
    }

    public static String todayAfter(String date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(String2Date(date));
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);
        return Date2String(cal.getTime());
    }

    public static String handleDate(int year, int monthOfYear, int dayOfMonth) {
        String month = String.valueOf(monthOfYear < 10 ? "0" + monthOfYear
                : monthOfYear);
        String day = String.valueOf(dayOfMonth < 10 ? "0" + dayOfMonth
                : dayOfMonth);
        return year + "-" + month + "-" + day;
    }

    public void restartApp(Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());  //结束进程之前可以把你程序的注销或者退出代码放在这段代码之前
    }
}
