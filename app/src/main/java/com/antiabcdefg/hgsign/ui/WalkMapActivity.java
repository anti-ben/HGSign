package com.antiabcdefg.hgsign.ui;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;
import com.antiabcdefg.hgsign.R;
import com.antiabcdefg.hgsign.bean.TimeBean;
import com.antiabcdefg.hgsign.utils.CommonUtil;
import com.antiabcdefg.hgsign.utils.DBManager;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;


public class WalkMapActivity extends AppCompatActivity implements LocationSource,
        AMapLocationListener {

    private int[] colors = new int[]{Color.argb(255, 0, 255, 0), Color.argb(255, 0, 150, 136), Color.argb(255, 255, 193, 7), Color.argb(255, 205, 220, 57)};

    private AMap aMap;
    private MapView mapView;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
//    private LBSTraceClient mTraceClient;

    private Button before;
    private Button today;
    private Button after;

    private ProgressDialog mypDialog = null;
    private DBManager dbManager;
    private volatile boolean isFirstZoom = true;

    private String dateTemp;
    private ArrayList<TimeBean> timeInfo;
    private ArrayList<ArrayList<LatLng>> LatLngInfo;
//    private ArrayList<ArrayList<TraceLocation>> TraceInfo;
//    private ConcurrentMap<Integer, TraceOverlay> mOverlayList = new ConcurrentHashMap<Integer, TraceOverlay>();

    private int count = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_mapshow);
        mapView = $(R.id.mapView);
        mapView.onCreate(savedInstanceState);// 此方法必须重写,下面一一绑定

        initView();
        initListener();
        initData();

        aMap.clear();
        drawMap(dateTemp);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("aaa", "onRestart()");
    }

    /**
     * 在onCreate,onRestart后面执行
     */
    @Override
    protected void onStart() {
        super.onStart();
        Log.i("aaa", "onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        Log.i("aaa", "onResume()");

    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        deactivate();
        Log.i("aaa", "onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("aaa", "onStop()");
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        dbManager.closeDB();
        if (null != mlocationClient) {
            mlocationClient.onDestroy();
        }
        Log.i("aaa", "onDestroy()");
    }

    protected void initView() {
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
        }
        before = $(R.id.btn_before);
        today = $(R.id.btn_today);
        after = $(R.id.btn_after);
    }

    protected void initData() {
        mypDialog = CommonUtil.getProcessDialog(WalkMapActivity.this, "正在绘图>>>");
        dbManager = new DBManager(WalkMapActivity.this);
//        TraceInfo = new ArrayList<ArrayList<TraceLocation>>();
        LatLngInfo = new ArrayList<>();
        timeInfo = new ArrayList<>();

//        mTraceClient = new LBSTraceClient(getApplicationContext());

        if (getIntent() != null)
            dateTemp = getIntent().getStringExtra("date");
    }

    protected void initListener() {
        before.setOnClickListener(v -> {
            aMap.clear();
            drawMap(CommonUtil.todayBefore(dateTemp));
            dateTemp = CommonUtil.todayBefore(dateTemp);
        });
        today.setOnClickListener(v -> {
            aMap.clear();
            drawMap(CommonUtil.Date2String(new Date()));
            dateTemp = CommonUtil.Date2String(new Date());
        });
        after.setOnClickListener(v -> {
            aMap.clear();
            drawMap(CommonUtil.todayAfter(dateTemp));
            dateTemp = CommonUtil.todayAfter(dateTemp);
        });
        aMap.setOnMapClickListener(latLng -> {
            if (count % 2 == 0) setBtnView(false);
            else setBtnView(true);
            count++;
        });
        aMap.setOnMarkerClickListener(marker -> false);
    }

    public void setBtnView(Boolean bool) {
        if (bool) {
            before.setVisibility(View.VISIBLE);
            today.setVisibility(View.VISIBLE);
            after.setVisibility(View.VISIBLE);
        } else {
            before.setVisibility(View.INVISIBLE);
            today.setVisibility(View.INVISIBLE);
            after.setVisibility(View.INVISIBLE);
        }
    }

    public void setUpMap() {
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(false);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                33.5460901204, 119.0362459272), 15));
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                .fromResource(R.mipmap.location_marker));
        myLocationStyle.strokeWidth(0);
        myLocationStyle.radiusFillColor(Color.alpha(0));
        myLocationStyle.strokeColor(Color.alpha(0));// 三个值为了周围透明,去除误差圈
        aMap.setMyLocationStyle(myLocationStyle);

    }

    private void drawMap(String date) {
        mypDialog.show();
        if (!TextUtils.isEmpty(date)) {
            LatLngInfo = dbManager.queryLocationList(date);
            LatLngInfo = dbManager.removeToLong(LatLngInfo);
            LatLngInfo = dbManager.correctLocations(LatLngInfo);
            LatLngInfo = dbManager.correcteZShape(LatLngInfo);
            timeInfo = dbManager.queryTimeList(date);
//          TraceInfo=dbManager.queryTraceList(date);
//            int size = TraceInfo.size();
//            if (size != 0) {
//                for (int i = 0; i < size; i++) {
//                    traceGrasp(1000 + i,TraceInfo.get(i));
//                }
//                mypDialog.dismiss();
//                CommonUtil.ToastLong(WalkMapActivity.this, "绘图完成");
//            } else {
//                mypDialog.dismiss();
//                CommonUtil.ToastLong(WalkMapActivity.this, "没有记录");
//            }

            if (LatLngInfo.size() != 0) {
                for (int i = 0, len = LatLngInfo.size(); i < len; i++) {
                    ArrayList<LatLng> lalotemp = LatLngInfo.get(i);
                    addMarkers(lalotemp.get(0), true, timeInfo.get(i).getStartTime(), i + 1);
                    upMap(lalotemp, colors[(int) (Math.random() * colors.length)]);
                    addMarkers(lalotemp.get(lalotemp.size() - 1), false, timeInfo.get(i).getEndTime(), i + 1);
                }
                mypDialog.dismiss();
                CommonUtil.ToastLong(WalkMapActivity.this, "绘图完成");
            } else {
                mypDialog.dismiss();
                CommonUtil.ToastLong(WalkMapActivity.this, "没有记录");
            }
        }

    }

    private void upMap(List<LatLng> points, int i) {
        aMap.addPolyline((new PolylineOptions()).addAll(points).geodesic(true)
                .useGradient(true).color(i).width(20));
    }

//    private void traceGrasp(int mSequenceLineID, List<TraceLocation> mTraceList) {
//        if (mOverlayList.containsKey(mSequenceLineID)) {
//            TraceOverlay overlay = mOverlayList.get(mSequenceLineID);
//            overlay.zoopToSpan();
//            int status = overlay.getTraceStatus();
//            String tipString = "";
//            if (status == TraceOverlay.TRACE_STATUS_PROCESSING) {
//                tipString = "该线路轨迹纠偏进行中...";
//            } else if (status == TraceOverlay.TRACE_STATUS_FINISH) {
//                tipString = "该线路轨迹已完成";
//            } else if (status == TraceOverlay.TRACE_STATUS_FAILURE) {
//                tipString = "该线路轨迹失败";
//            } else if (status == TraceOverlay.TRACE_STATUS_PREPARE) {
//                tipString = "该线路轨迹纠偏已经开始";
//            }
//            CommonUtil.ToastShort(WalkMapActivity.this, tipString);
//            return;
//        }
//        TraceOverlay mTraceOverlay = new TraceOverlay(aMap);
//        mOverlayList.put(mSequenceLineID, mTraceOverlay);
//        List<LatLng> mapList = traceLocationToMap(mTraceList);
//        mTraceOverlay.setProperCamera(mapList);
//        mTraceClient.queryProcessedTrace(mSequenceLineID, mTraceList, LBSTraceClient.TYPE_AMAP, this);
//
//    }

    private void addMarkers(LatLng pointTemp, boolean isStart, String time, int pointTimes) {
        if (isStart)
            aMap.addMarker(new MarkerOptions()
                    .position(pointTemp)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.start))
                    .snippet(time)
                    .title("第" + pointTimes + "次轨迹起点"));
        else
            aMap.addMarker(new MarkerOptions()
                    .position(pointTemp)
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.end))
                    .snippet(time)
                    .title("第" + pointTimes + "次轨迹终点"));
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
        Log.i("aaa", "onSaveInstanceState()");
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null && amapLocation.getErrorCode() == 0) {
                // mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
                if (isFirstZoom) {
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                    isFirstZoom = false;
                }
                mlocationClient.stopLocation();// 定位成功则定位一次
            }

        }
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
            // 设置定位监听
            mlocationClient.setLocationListener(this);
            // 设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            // 设置定位参数
            mLocationOption.setWifiScan(true);
            mLocationOption.setInterval(7000);
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            mLocationOption.setGpsFirst(true);
            // mLocationOption.setKillProcess(true);
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    public <T extends View> T $(int id) {
        return (T) findViewById(id);
    }

//    @Override
//    public void onRequestFailed(int lineID, String errorInfo) {
//        CommonUtil.ToastShort(WalkMapActivity.this, errorInfo);
//        if (mOverlayList.containsKey(lineID)) {
//            TraceOverlay overlay = mOverlayList.get(lineID);
//            overlay.setTraceStatus(TraceOverlay.TRACE_STATUS_FAILURE);
//        }
//    }

//    @Override
//    public void onTraceProcessing(int lineID, int index, List<LatLng> segments) {
//        if (segments == null) {
//            return;
//        }
//        if (mOverlayList.containsKey(lineID)) {
//            TraceOverlay overlay = mOverlayList.get(lineID);
//            overlay.setTraceStatus(TraceOverlay.TRACE_STATUS_PROCESSING);
//            overlay.add(segments);
//        }
//    }

//    @Override
//    public void onFinished(int lineID, List<LatLng> linepoints, int distance, int watingtime) {
//        if (mOverlayList.containsKey(lineID)) {
//            TraceOverlay overlay = mOverlayList.get(lineID);
//            overlay.setTraceStatus(TraceOverlay.TRACE_STATUS_FINISH);
//        }
//    }

//    public List<LatLng> traceLocationToMap(List<TraceLocation> traceLocationList) {
//        List<LatLng> mapList = new ArrayList<LatLng>();
//        for (TraceLocation location : traceLocationList) {
//            LatLng latlng = new LatLng(location.getLatitude(),
//                    location.getLongitude());
//            mapList.add(latlng);
//        }
//        return mapList;
//    }

}