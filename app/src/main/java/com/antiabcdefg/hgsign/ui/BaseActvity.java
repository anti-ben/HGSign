package com.antiabcdefg.hgsign.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import com.antiabcdefg.hgsign.R;

public abstract class BaseActvity extends AppCompatActivity {

    protected ToolbarHelper mToolbarHelper;
    private boolean isExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        setContentView(getContentViewId());
        initToolBar();
        initView();
        initData();
        initListener();
    }

    protected void initToolBar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            //隐藏/显示Tittle
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            //决定左上角的图标是否可以点击
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            //隐藏/显示返回箭头
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mToolbarHelper = new ToolbarHelper(toolbar);
            hanldeToolbar(mToolbarHelper);
        }

    }

    protected abstract void initView();

    protected void initData(){
        if (getIntent() != null) {
            handleIntent(getIntent());
        }
    }

    public <T extends View> T $(int id){
        return (T) findViewById(id);
    }

    protected void initListener(){}

    protected abstract int getContentViewId();

    protected void hanldeToolbar(ToolbarHelper toolbarHelper) {}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isExit) {
                this.finish();
            } else {
                Snackbar.make(getWindow().getDecorView(), "再按一次退出", Snackbar.LENGTH_LONG).show();
                isExit = true;
                new Handler().postDelayed(() -> isExit = false, 2000);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void handleIntent(Intent intent) {}

    public static class ToolbarHelper {

        private Toolbar mToolbar;

        public ToolbarHelper(Toolbar toolbar) {
            this.mToolbar = toolbar;
        }

        public Toolbar getToolbar() {
            return mToolbar;
        }

    }
}
