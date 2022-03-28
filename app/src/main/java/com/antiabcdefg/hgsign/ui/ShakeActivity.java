package com.antiabcdefg.hgsign.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.antiabcdefg.hgsign.R;
import com.antiabcdefg.hgsign.bean.DataResponseEntity;
import com.antiabcdefg.hgsign.bean.LocationBean;
import com.antiabcdefg.hgsign.net.ApiStores;
import com.antiabcdefg.hgsign.net.HttpMethods;
import com.antiabcdefg.hgsign.service.GpsRouteService;
import com.antiabcdefg.hgsign.service.StepCounterService;
import com.antiabcdefg.hgsign.utils.CommonUtil;
import com.antiabcdefg.hgsign.utils.MyAMAPLocation;
import com.antiabcdefg.hgsign.utils.MyApplication;
import com.antiabcdefg.hgsign.utils.MySound;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RuntimePermissions
public class ShakeActivity extends BaseActvity {

    public MyApplication myApplication;
    private SharedPreferences preference;
    private ProgressDialog progressDialog;
    private MySound sound;
    private String methods;
    private MyAMAPLocation locationClient;
    private Intent StepService;
    private Intent GPSService;
    private Call<DataResponseEntity> GPSCall;
    private Call<DataResponseEntity> MacCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (preference.getBoolean("isfirstDiaLogShake", true)) {
            DialogTip();
            preference.edit().putBoolean("isfirstDiaLogShake", false).apply();
        }
        if (!CommonUtil.isGpsOPen(ShakeActivity.this))
            DialogGPS();

    }

    protected void initView() {

    }

    protected void initData() {
        super.initData();
        myApplication = (MyApplication) getApplication();
        preference = getSharedPreferences("preference", MODE_PRIVATE);
        locationClient = new MyAMAPLocation(ShakeActivity.this);
        progressDialog = CommonUtil.getProcessDialog(ShakeActivity.this, "正在签到>>>");
        StepService = new Intent(this, StepCounterService.class);
        GPSService = new Intent(this, GpsRouteService.class);
        sound = new MySound(this, R.raw.one);
    }

    @Override
    protected void initListener() {
        ((ImageView) findViewById(R.id.shake)).setOnClickListener((View.OnClickListener) view -> sendData());
    }

    @Override
    protected void handleIntent(Intent intent) {
        methods = intent.getStringExtra("methods");
    }


    @Override
    protected void hanldeToolbar(ToolbarHelper toolbarHelper) {
        super.hanldeToolbar(toolbarHelper);
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_shake;
    }

    public void DialogTip() {
        new AlertDialog.Builder(ShakeActivity.this).
                setTitle("注意！").
                setMessage("1.点击学校LOGO进行考勤\n" + "2.请保持APP在前台，防止因为APP的关闭而导致考勤失败!\n").
                setPositiveButton("确定", (dialog, which) -> {
                }).create().show();
    }

    public void DialogResult(String msg) {
        new AlertDialog.Builder(ShakeActivity.this).
                setMessage(msg).
                setPositiveButton("确定", (dialog, which) -> {
                    if (!StepCounterService.isOpen) {
                        startService(StepService);
                        startService(GPSService);
                    }
                }).create().show();
    }

    public void DialogGPS() {
        new AlertDialog.Builder(ShakeActivity.this).
                setTitle("是否打开GPS").
                setMessage("注意:打开GPS会定位更加准确").
                setPositiveButton("打开", (dialog, which) -> {
                    Intent intent = new Intent(
                            Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, 0); // 此为设置完成后返回到获取界面
                }).
                setNegativeButton("算了", (dialog, which) -> {
                }).create().show();
    }


    private void sendData() {
        progressDialog.show();
     //   System.out.println("aaaa+" + methods + "+" + getRealTime() + "+" + StepCounterService.FIRST_STEP + "+" + StepCounterService.CURRENT_STEP);
        if (methods.equalsIgnoreCase("wifi")) {
            postMac(HttpMethods.getInstance().getApiStoreWrite(), myApplication.getUserBean().getNumber(), myApplication.getMacInfoResponseEntity().getLocation(), getRealTime(), myApplication.getMacInfoResponseEntity().getReturnRouterMac());
        } else
            ShakeActivityPermissionsDispatcher.gpsSignWithPermissionCheck(ShakeActivity.this);
    }


    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void gpsSign() {
        progressDialog.show();
        locationClient = new MyAMAPLocation(MyApplication.getContext());
        locationClient.setOnResponseToLocationListener((street, latitude, longitude, speed, bearing, time, satellites, altitude) -> {
            if (latitude != 0 && longitude != 0) {
                LocationBean locationBean = new LocationBean();
                locationBean.setLatitude(latitude + "");
                locationBean.setLongitude(longitude + "");
                locationBean.setDescribe(street);
                locationClient.stop();
                postGPS(HttpMethods.getInstance().getApiStoreWrite(), myApplication.getUserBean().getNumber(), locationBean.getLatitude(), locationBean.getLongitude(), locationBean.getDescribe(), getRealTime(), StepCounterService.CURRENT_STEP + "");
            } else {
                locationClient.stop();
                progressDialog.dismiss();
                CommonUtil.ToastLong(MyApplication.getContext(), "位置获取出现错误，请重试");
            }
        });
        locationClient.start();
    }

    public void postGPS(ApiStores apiStores, String userid, String locationY, String locationX, String locationName, String checkTime, String steps) {
        GPSCall = apiStores.postGPS(userid, locationY, locationX, locationName, checkTime, steps);
        GPSCall.enqueue(new Callback<DataResponseEntity>() {
            @Override
            public void onResponse(@NonNull Call<DataResponseEntity> call, @NonNull Response<DataResponseEntity> response) {
                progressDialog.dismiss();
                if(response.body().getMsg().equalsIgnoreCase("true")) {
                    sound.play();
                    DialogResult(response.body().getDetails());
                }else  DialogResult("出现错误，请重试");
            }

            @Override
            public void onFailure(@NonNull Call<DataResponseEntity> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                CommonUtil.ToastShort(MyApplication.getContext(), "网络错误，请重试");
            }
        });
    }

    public void postMac(ApiStores apiStores, String userid, String address, String checkTime, String wifiMac) {
        MacCall = apiStores.postMac(userid, address, checkTime, wifiMac);
        MacCall.enqueue(new Callback<DataResponseEntity>() {
            @Override
            public void onResponse(@NonNull Call<DataResponseEntity> call, @NonNull Response<DataResponseEntity> response) {
                progressDialog.dismiss();
                if(response.body().getMsg().equalsIgnoreCase("true")) {
                    sound.play();
                    DialogResult(response.body().getDetails());
                }else DialogResult("出现错误，请重试");
            }

            @Override
            public void onFailure(@NonNull Call<DataResponseEntity> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                CommonUtil.ToastShort(MyApplication.getContext(), "网络错误，请重试");
            }
        });
    }


    public String getRealTime() {
        long intervalTime = (new Date().getTime()) - (myApplication.getUserBean().getTempTime());//毫秒
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(format.parse(myApplication.getUserBean().getGetTime()));
            cal.add(Calendar.SECOND, (int) intervalTime / 1000);
            return format.format(cal.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ShakeActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void onDestroy() {
        if (CommonUtil.isServiceRunning(ShakeActivity.this, "com.antiabcdefg.hgsign.service.StepCounterService") && CommonUtil.isServiceRunning(ShakeActivity.this, "com.antiabcdefg.hgsign.service.GpsRouteService")) {
            stopService(StepService);
            stopService(GPSService);
        }
        if (MacCall != null && GPSCall != null) {
            MacCall.cancel();
            GPSCall.cancel();
        }
        super.onDestroy();
    }

}
