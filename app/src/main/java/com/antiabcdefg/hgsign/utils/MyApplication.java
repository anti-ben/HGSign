package com.antiabcdefg.hgsign.utils;

import android.app.Application;
import android.content.Context;

import com.antiabcdefg.hgsign.bean.MacInfoResponseEntity;
import com.antiabcdefg.hgsign.bean.UserBean;


public class MyApplication extends Application {
    public static Context context;
    public UserBean userBean;
    public MacInfoResponseEntity macInfoResponseEntity;


    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }

    public UserBean getUserBean() {
        return userBean;
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    public MacInfoResponseEntity getMacInfoResponseEntity() {
        return macInfoResponseEntity;
    }

    public void setMacInfoResponseEntity(MacInfoResponseEntity macInfoResponseEntity) {
        this.macInfoResponseEntity = macInfoResponseEntity;
    }
}
