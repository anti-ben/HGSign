package com.antiabcdefg.hgsign.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.antiabcdefg.hgsign.R;
import com.antiabcdefg.hgsign.bean.DeviceResponseEntity;
import com.antiabcdefg.hgsign.bean.UnBindSiteEntity;
import com.antiabcdefg.hgsign.bean.UserEntity;
import com.antiabcdefg.hgsign.net.ApiStores;
import com.antiabcdefg.hgsign.net.HttpMethods;
import com.antiabcdefg.hgsign.utils.CommonUtil;
import com.antiabcdefg.hgsign.utils.MyApplication;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


@RuntimePermissions
public class LoginActivity extends BaseActvity {

    public MyApplication myApplication;
    private TextView number;
    private TextView pwd;
    private Button btn;
    private ProgressDialog mypDialog = null;
    private Call<UserEntity> userEntityCall;
    private Call<DeviceResponseEntity> deviceResponseEntityCall;
    private Call<UnBindSiteEntity> unBindSiteEntityCall;
    private SharedPreferences preference;
    private String nameTemp = "";
    private String pwdTemp = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void initView() {
        number = $(R.id.input_number);
        pwd = $(R.id.input_password);
        btn = $(R.id.login_btn);
    }

    protected void initData() {
        super.initData();
        myApplication = (MyApplication) getApplication();
        preference = getSharedPreferences("preference", MODE_PRIVATE);
        mypDialog = CommonUtil.getProcessDialog(LoginActivity.this, "正在登陆>>>");
    }

    protected void initListener() {
        btn.setOnClickListener(view -> {
            if (CommonUtil.isConnected(LoginActivity.this)) {
                if (!checkEmpty()) {
                    mypDialog.show();
//                      checkDevice(HttpMethods.getInstance().getApiStoreRead(), number.getText().toString().trim(), deviceID);
                    getUser(HttpMethods.getInstance().getApiStoreRead(), number.getText().toString().trim(), pwd.getText().toString().trim());
                } else CommonUtil.ToastShort(LoginActivity.this, "账号与密码不能为空");
            } else CommonUtil.ToastShort(MyApplication.getContext(), "网络错误，请重试");
        });
    }

    @Override
    protected void hanldeToolbar(ToolbarHelper toolbarHelper) {
        super.hanldeToolbar(toolbarHelper);
        Toolbar toolbar = toolbarHelper.getToolbar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        toolbar.setTitle("登陆");
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_login;
    }

    public void showUnBindInfo(String tip) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setMessage(tip);
        builder.setPositiveButton("前往解绑", (dialog, which) -> getUnBindSite(HttpMethods.getInstance().getApiStoreRead()));
        builder.setNegativeButton("取消", (dialog, which) -> {
        });
        builder.create().show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mypDialog.dismiss();
        retrieveSession();
        LoginActivityPermissionsDispatcher.verifyDeviceWithPermissionCheck(LoginActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mypDialog.dismiss();
        saveSessionTemp();
    }

    /* 方法前面不能带有private修饰符*/
    @NeedsPermission(Manifest.permission.READ_PHONE_STATE)
    void verifyDevice() {
        String deviceID = CommonUtil.getDeviceID(LoginActivity.this);
        if (deviceID.length() == 0) {
            mypDialog.dismiss();
            CommonUtil.ToastLong(MyApplication.getContext(), "无法读取到设备ID");
            this.finish();
        }
    }

    public void getUser(ApiStores apiStores, String username, String password) {
        userEntityCall = apiStores.getUser(username, password);
        userEntityCall.enqueue(new Callback<UserEntity>() {
            @Override
            public void onResponse(@NonNull Call<UserEntity> call, @NonNull Response<UserEntity> response) {
                if (response.body().getUserInfo().getMsg().equalsIgnoreCase("true")) {
                    mypDialog.dismiss();
                    CommonUtil.setUser(myApplication, response.body());
                    saveSession();
                    next();
                } else {
                    mypDialog.dismiss();
                    CommonUtil.ToastLong(MyApplication.getContext(), "账号或密码错误,请重新输入!\n" + "(注意：密码默认为身份证后六位!)");
                    resetSession();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserEntity> call, @NonNull Throwable t) {
                mypDialog.dismiss();
                CommonUtil.ToastShort(MyApplication.getContext(), "网络错误，请重试");
            }
        });
    }

    public void checkDevice(ApiStores apiStores, String username, String deviceNum) {
        deviceResponseEntityCall = apiStores.checkDevice(username, deviceNum);
        deviceResponseEntityCall.enqueue(new Callback<DeviceResponseEntity>() {
            @Override
            public void onResponse(@NonNull Call<DeviceResponseEntity> call, @NonNull Response<DeviceResponseEntity> response) {
                if (response.body().getDeviceResult().equalsIgnoreCase("true"))
                    getUser(HttpMethods.getInstance().getApiStoreRead(), number.getText().toString().trim(), pwd.getText().toString().trim());
                else {
                    mypDialog.dismiss();
                    showUnBindInfo(response.body().getMsg() + " \n" + "请到答题网站处理后解除绑定。");
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeviceResponseEntity> call, @NonNull Throwable t) {
                mypDialog.dismiss();
                CommonUtil.ToastShort(MyApplication.getContext(), "网络错误，请重试");
            }
        });
    }

    public void getUnBindSite(ApiStores apiStores) {
        unBindSiteEntityCall = apiStores.getUnBindSite();
        unBindSiteEntityCall.enqueue(new Callback<UnBindSiteEntity>() {
            @Override
            public void onResponse(@NonNull Call<UnBindSiteEntity> call, @NonNull Response<UnBindSiteEntity> response) {
                mypDialog.dismiss();
                try {
                    Uri uri = Uri.parse(response.body().getUrl());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                } catch (Exception e) {
                    mypDialog.dismiss();
                    CommonUtil.ToastLong(MyApplication.getContext(), "解绑url 格式错误!");
                }
            }

            @Override
            public void onFailure(@NonNull Call<UnBindSiteEntity> call, @NonNull Throwable t) {
                mypDialog.dismiss();
                CommonUtil.ToastShort(MyApplication.getContext(), "网络错误，请重试");
            }
        });
    }

    public boolean checkEmpty() {
        return TextUtils.isEmpty(number.getText().toString())
                || TextUtils.isEmpty(pwd.getText().toString());

    }

    public void saveSession() {
        Editor edit = preference.edit();
        edit.putString("number", number.getText().toString().trim());
        edit.putString("pwd", pwd.getText().toString().trim());
        edit.putBoolean("isfirst", false);
        edit.apply();
    }

    public void saveSessionTemp() {
        nameTemp = number.getText().toString().trim();
        pwdTemp = pwd.getText().toString().trim();
    }

    private void retrieveSession() {
        number.setText(nameTemp);
        pwd.setText(pwdTemp);
    }

    private void resetSession() {
        number.setText("");
        pwd.setText("");
        number.setFocusable(true);
    }

    public void next() {
        Intent intent = new Intent(this, MainActivity.class);
        this.finish();
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LoginActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void onDestroy() {
        if (deviceResponseEntityCall != null && unBindSiteEntityCall != null && userEntityCall != null) {
            userEntityCall.cancel();
            deviceResponseEntityCall.cancel();
            unBindSiteEntityCall.cancel();
        }
        super.onDestroy();
    }
}
