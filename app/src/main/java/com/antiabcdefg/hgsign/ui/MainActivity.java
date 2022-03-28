package com.antiabcdefg.hgsign.ui;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.antiabcdefg.hgsign.R;
import com.antiabcdefg.hgsign.adapter.MainActivityAdapter;
import com.antiabcdefg.hgsign.bean.MacInfoResponseEntity;
import com.antiabcdefg.hgsign.bean.NotificationEntity;
import com.antiabcdefg.hgsign.bean.UserEntity;
import com.antiabcdefg.hgsign.net.ApiStores;
import com.antiabcdefg.hgsign.net.HttpMethods;
import com.antiabcdefg.hgsign.utils.CommonUtil;
import com.antiabcdefg.hgsign.utils.MyApplication;

import java.util.ArrayList;
import java.util.Calendar;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RuntimePermissions
public class MainActivity extends BaseActvity {

    public MyApplication myApplication;
    private SharedPreferences preference;
    private ListView mListView;
    private ArrayAdapter<String> arrayAdapter;
    private NavigationView mNavView;
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private FloatingActionsMenu menuMultipleActions;
    private FloatingActionButton gpsBtn;
    private FloatingActionButton wifiBtn;

    private ProgressDialog progressDialog;
    private SuperRecyclerView mRecyclerView;
    private MainActivityAdapter mainActivityAdapter;
    private ArrayList<String> mNotifitioninfos;
    private Call<NotificationEntity> notificationEntityCall;
    private Call<MacInfoResponseEntity> macInfoResponseEntityCall;
    private Call<UserEntity> userEntityCall;

    private BroadcastReceiver netReceiver;

    private boolean isExit;
    private boolean isRefreshing;

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            arrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1,
                    CommonUtil.getWifiName(MainActivity.this));
            mListView.setAdapter(arrayAdapter);
            arrayAdapter.notifyDataSetChanged();
            handler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initNetReceiver();

        if (preference.getBoolean("isfirstDiaLogTip", true)) {
            DialogTip();
            preference.edit().putBoolean("isfirstDiaLogTip", false).apply();
        }

        // 第一次进入页面的时候显示加载进度条
        mRecyclerView.post(() -> mRecyclerView.setRefreshing(true));

        if (checkUserNull()) {
            getNotification(HttpMethods.getInstance().getApiStoreRead(), myApplication.getUserBean().getNumber(), "android");
        }

        setAdapter();

    }

    protected void initView() {
        mNavView = $(R.id.nav_view);
        mDrawerLayout = $(R.id.drawer_layout);
        menuMultipleActions = $(R.id.multiple_actions);
        gpsBtn = $(R.id.btn_gps);
        wifiBtn = $(R.id.btn_wifi);
        progressDialog = CommonUtil.getProcessDialog(MainActivity.this, "正在获取>>>>");

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        //设置左上角显示三道横线
        toggle.syncState();
//        setDrawerLeftEdgeSize(this, mDrawerLayout, 0.5f);

        mRecyclerView = $(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setRefreshingColorResources(android.R.color.holo_blue_light, android.R.color.holo_orange_light, android.R.color.holo_green_light, android.R.color.holo_red_light);
    }

    protected void initData() {
        super.initData();
        myApplication = (MyApplication) getApplication();
        preference = getSharedPreferences("preference", MODE_PRIVATE);
        mNotifitioninfos = new ArrayList<>();
    }

    protected void initListener() {
        mNavView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_item1:
                    break;
                case R.id.nav_item2:
                    diaLogDate();
                    break;
                case R.id.nav_item3:
                    next("", "", SettingsActivity.class);
                    break;
                case R.id.nav_item4:
                    showDelete("确定要注销?");
                    break;
            }
            //隐藏NavigationView
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        mRecyclerView.setRefreshListener(() -> {
            if (!isRefreshing) {
                isRefreshing = true;
//                    new Handler().postDelayed(new Runnable() {
//                        public void run() {
                if (checkUserNull())
                    getNotification(HttpMethods.getInstance().getApiStoreRead(), myApplication.getUserBean().getNumber(), "android");
//                        }
//                    }, 700);
            }
        });

        gpsBtn.setOnClickListener(view -> {
            if (checkUserNull())
                gpsSign();
            else {
                CommonUtil.ToastLong(MainActivity.this, "用户登陆错误，请重试");
                getUser(HttpMethods.getInstance().getApiStoreRead(), preference.getString("number", ""), preference.getString("pwd", ""));
            }
        });

        wifiBtn.setOnClickListener(view -> {
            if (checkUserNull())
                MainActivityPermissionsDispatcher.wifiSignWithPermissionCheck(MainActivity.this);
            else {
                CommonUtil.ToastLong(MainActivity.this, "用户登陆错误，请重试");
                getUser(HttpMethods.getInstance().getApiStoreRead(), preference.getString("number", ""), preference.getString("pwd", ""));
            }
        });
    }

    private void initNetReceiver() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        // 实例化广播接收器
        netReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                    //获取联网状态的NetworkInfo对象
                    NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                    if (info != null) {
                        //如果当前的网络连接成功并且网络连接可用
                        if (NetworkInfo.State.CONNECTED == info.getState() && info.isAvailable()) {
                            if (info.getType() == ConnectivityManager.TYPE_WIFI
                                    || info.getType() == ConnectivityManager.TYPE_MOBILE) {
                                getUser(HttpMethods.getInstance().getApiStoreRead(), preference.getString("number", ""), preference.getString("pwd", ""));
                            }
                        }
                    }
                }
            }
        };
        // 注册计步服务广播
        registerReceiver(netReceiver, filter);
    }

    public void setAdapter() {
        if (mainActivityAdapter == null) {
            mainActivityAdapter = new MainActivityAdapter(mNotifitioninfos);
            mRecyclerView.setAdapter(mainActivityAdapter);
        } else mainActivityAdapter.notifyDataSetChanged();
    }

    @Override
    protected void hanldeToolbar(ToolbarHelper toolbarHelper) {
        super.hanldeToolbar(toolbarHelper);

        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_main;
    }

    public void DialogTip() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("首次提醒")
                .setMessage("1.建议室内用WIFI签到,室外用GPS签到,可提高签到成功率\n" +
                        "2.室外GPS签到时，请保持在签到界面不要退出\n" + "3.左侧侧边栏可以注销")
                .setPositiveButton("确定", (dialog, which) -> {

                }).create().show();
    }

    public void getUser(ApiStores apiStores, String username, String password) {
        userEntityCall = apiStores.getUser(username, password);
        userEntityCall.enqueue(new Callback<UserEntity>() {
            @Override
            public void onResponse(@NonNull Call<UserEntity> call, @NonNull Response<UserEntity> response) {
                mRecyclerView.setRefreshing(false);
                isRefreshing = false;
                if (response.body().getUserInfo().getMsg().equalsIgnoreCase("true")) {
                    CommonUtil.setUser(myApplication, response.body());
                } else {
                    CommonUtil.ToastLong(MyApplication.getContext(), "账号或密码错误\n" + "(注意：密码默认为身份证后六位!)");
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserEntity> call, @NonNull Throwable t) {
                mRecyclerView.setRefreshing(false);
                isRefreshing = false;
                CommonUtil.ToastShort(MyApplication.getContext(), "网络错误，请重试");
            }
        });
    }

    public void getNotification(ApiStores apiStores, String userid, String device) {
        notificationEntityCall = apiStores.getNotification(userid, device);
        notificationEntityCall.enqueue(new Callback<NotificationEntity>() {
            @Override
            public void onResponse(@NonNull Call<NotificationEntity> call, @NonNull Response<NotificationEntity> response) {
                mRecyclerView.setRefreshing(false);
                isRefreshing = false;
                mNotifitioninfos.clear();
                for (NotificationEntity.AllresultBean allresultBean : response.body().getAllresult())
                    mNotifitioninfos.add(allresultBean.getInfo());
                setAdapter();
            }

            @Override
            public void onFailure(@NonNull Call<NotificationEntity> call, @NonNull Throwable t) {
                mRecyclerView.setRefreshing(false);
                isRefreshing = false;
                CommonUtil.ToastShort(MyApplication.getContext(), "网络错误，请重试");
            }
        });
    }

    private void gpsSign() {
        progressDialog.dismiss();
        next("gps", "", ShakeActivity.class);
    }

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void wifiSign() {
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View view = layoutInflater.inflate(R.layout.dialog_wifilist, null);

        mListView = (ListView) view.findViewById(R.id.wifilist);
        arrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1,
                CommonUtil.getWifiName(MainActivity.this));
        mListView.setAdapter(arrayAdapter);

        handler.postDelayed(runnable, 500);

        if (CommonUtil.getWifiName(MainActivity.this).size() == 0)
            CommonUtil.ToastLong(MyApplication.getContext(), "列表为空，你可以继续签到");

        new AlertDialog.Builder(MainActivity.this).
                setTitle("当前的WIFI列表").
                setView(view).
                setOnDismissListener(dialog -> handler.removeCallbacks(runnable)).
                setPositiveButton("开始签到", (dialog, which) -> {
                    if (CommonUtil.isValidWLName(MainActivity.this))
                        CommonUtil.closeWifi(MainActivity.this);
                    else
                        checkMac(HttpMethods.getInstance().getApiStoreRead(), myApplication.getUserBean().getNumber(), CommonUtil.getValidate(MainActivity.this));

                }).create().show();
    }

    public void checkMac(ApiStores apiStores, String username, String routerMac) {
        progressDialog.show();
        macInfoResponseEntityCall = apiStores.checkMac(username, routerMac);
        macInfoResponseEntityCall.enqueue(new Callback<MacInfoResponseEntity>() {
            @Override
            public void onResponse(@NonNull Call<MacInfoResponseEntity> call, @NonNull Response<MacInfoResponseEntity> response) {
                if (response.body().getMacRes().equalsIgnoreCase("true")) {
                    progressDialog.dismiss();
                    myApplication.setMacInfoResponseEntity(response.body());
                    next("wifi", "", ShakeActivity.class);
                } else gpsSign();
            }

            @Override
            public void onFailure(@NonNull Call<MacInfoResponseEntity> call, @NonNull Throwable t) {
                CommonUtil.ToastShort(MyApplication.getContext(), "网络错误，请重试");
            }
        });
    }

    public void delete() {
        SharedPreferences.Editor edit = preference.edit();
        edit.putString("number", "");
        edit.putString("pwd", "");
        edit.putBoolean("isfirst", true);
        edit.putBoolean("isfirstDiaLogTip", true);
        edit.apply();

        CommonUtil.ToastShort(MyApplication.getContext(), "注销成功");

        Intent intent = new Intent(this, LoginActivity.class);
        this.finish();
        startActivity(intent);
    }

    public void showDelete(String tip) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(tip);
        builder.setPositiveButton("确定", (dialog, which) -> delete());
        builder.setNegativeButton("取消", (dialog, which) -> {
        });
        builder.create().show();
    }

    private void diaLogDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> next("", CommonUtil.handleDate(year1, monthOfYear + 1, dayOfMonth), WalkMapActivity.class), year, month, day).show();
    }

    public boolean checkUserNull() {
        return myApplication.getUserBean() != null;
    }

    public void next(String methods, String date, Class<?> cls) {
        progressDialog.dismiss();
        Intent intent = new Intent(this, cls);
        intent.putExtra("methods", methods);
        intent.putExtra("date", date);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (menuMultipleActions.isExpanded())
                menuMultipleActions.collapse();
            else if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            } else {
                if (isExit) {
                    this.finish();
                } else {
                    Snackbar.make((CoordinatorLayout) findViewById(R.id.main_content), "再按一次退出", Snackbar.LENGTH_LONG).show();
                    isExit = true;
                    new Handler().postDelayed(() -> isExit = false, 2000);
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(runnable);
        unregisterReceiver(netReceiver);
        if (macInfoResponseEntityCall != null && notificationEntityCall != null && userEntityCall != null) {
            macInfoResponseEntityCall.cancel();
            notificationEntityCall.cancel();
            userEntityCall.cancel();
        }
        super.onDestroy();
    }

}
