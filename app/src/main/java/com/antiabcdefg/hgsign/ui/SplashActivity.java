package com.antiabcdefg.hgsign.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.ImageView;

import com.antiabcdefg.hgsign.R;
import com.antiabcdefg.hgsign.bean.UserEntity;
import com.antiabcdefg.hgsign.net.ApiStores;
import com.antiabcdefg.hgsign.net.HttpMethods;
import com.antiabcdefg.hgsign.utils.CommonUtil;
import com.antiabcdefg.hgsign.utils.MyApplication;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SplashActivity extends AppCompatActivity {
    public MyApplication myApplication;
    private SharedPreferences preference;
    private Call<UserEntity> userEntityCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        myApplication = (MyApplication) getApplication();
        preference = getSharedPreferences("preference", MODE_PRIVATE);

        if (!CommonUtil.isConnected(SplashActivity.this)) {
            CommonUtil.ToastLong(MyApplication.getContext(), "网络异常，请检查网络");
            new Handler().postDelayed(() -> CommonUtil.openWifi(SplashActivity.this), 1300);

        } else
            getUser(HttpMethods.getInstance().getApiStoreRead(), preference.getString("number", ""), preference.getString("pwd", ""));

        ((ImageView) findViewById(R.id.logo)).postDelayed((Runnable) () -> {
            if (!preference.getBoolean("isfirst", true))
                next(MainActivity.class);
            else next(LoginActivity.class);
        }, 4000);

    }

    public void getUser(ApiStores apiStores, String username, String password) {
        userEntityCall = apiStores.getUser(username, password);
        userEntityCall.enqueue(new Callback<UserEntity>() {
            @Override
            public void onResponse(@NonNull Call<UserEntity> call, @NonNull Response<UserEntity> response) {
                if (response.body().getUserInfo().getMsg().equalsIgnoreCase("true")) {
                    CommonUtil.setUser(myApplication, response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserEntity> call, @NonNull Throwable t) {
                CommonUtil.ToastShort(MyApplication.getContext(), "网络错误，请重试");
            }
        });
    }

    public void next(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        this.finish();
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            this.finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy() {
        if (userEntityCall != null) {
            userEntityCall.cancel();
        }
        super.onDestroy();
    }

}
