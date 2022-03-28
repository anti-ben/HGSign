package com.antiabcdefg.hgsign.utils;

import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;

public class MyAMAPLocation implements AMapLocationListener {

    private AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption aMapLocationClientOption = null;
    private OnResponseToLocationListener onResponseToLocationListener = null;
    private volatile boolean isOpen = false;

    public MyAMAPLocation(Context context) {
        mLocationClient = new AMapLocationClient(context);
        aMapLocationClientOption = new AMapLocationClientOption();
        mLocationClient.setLocationListener(this);
        initLocation();
    }

    private void initLocation() {
        aMapLocationClientOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
        aMapLocationClientOption.setWifiActiveScan(true);
        /**
         * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位，可能会导致定位延迟30秒
         * 注意：只有在高精度模式下的单次定位有效，其他方式无效
         */
//        aMapLocationClientOption.setGpsFirst(true);
        //设置是否等待设备wifi刷新，如果设置为true,会自动变为单次定位，持续定位时不要使用
//        aMapLocationClientOption.setOnceLocationLatest(true);
        aMapLocationClientOption.setLocationCacheEnable(true);
        aMapLocationClientOption.setInterval(1000);//小于1000不生效
        mLocationClient.setLocationOption(aMapLocationClientOption);
    }


    public synchronized void start() {
        mLocationClient.startLocation();
        isOpen = true;
    }

    public synchronized void stop() {
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
            mLocationClient = null;
            aMapLocationClientOption = null;
        }
        isOpen = false;
    }

    public synchronized boolean isOpen() {
        return isOpen;
    }

    @Override
    public void onLocationChanged(AMapLocation location) {
        if (onResponseToLocationListener != null) {
            onResponseToLocationListener.OnResponse(location.getAddress(), location.getLatitude(),
                    location.getLongitude(), location.getSpeed(), location.getBearing(), location.getTime(), location.getSatellites(), location.getAltitude());
        }
    }

    public void setOnResponseToLocationListener(
            OnResponseToLocationListener onResponseToLocationListener) {
        this.onResponseToLocationListener = onResponseToLocationListener;
    }

    public interface OnResponseToLocationListener {
        void OnResponse(String street, double latitude, double longitude, float speed, float bearing, long time, int satellites, double altitude);
    }

}
