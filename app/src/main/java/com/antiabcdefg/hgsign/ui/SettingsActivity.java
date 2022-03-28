package com.antiabcdefg.hgsign.ui;

import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import androidx.appcompat.widget.Toolbar;
import android.view.KeyEvent;

import com.antiabcdefg.hgsign.R;
import com.antiabcdefg.hgsign.utils.MyApplication;


public class SettingsActivity extends BaseActvity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            SettingsFragment mSettingsFragment = new SettingsFragment();
            replaceFragment(R.id.settings_container, mSettingsFragment);
        }
    }

    @Override
    protected void initView() {

    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_settings;
    }

    @Override
    protected void hanldeToolbar(ToolbarHelper toolbarHelper) {
        super.hanldeToolbar(toolbarHelper);
        Toolbar toolbar = toolbarHelper.getToolbar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle("设置");
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void replaceFragment(int viewId, android.app.Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(viewId, fragment).commit();
    }

    public static class SettingsFragment extends PreferenceFragment {

        private static final String CHECK_ISFORCE_KEY = "check_preference";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            SwitchPreference switchPreference = ((SwitchPreference) findPreference(CHECK_ISFORCE_KEY));
            switchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (preference.getKey().equals(CHECK_ISFORCE_KEY)) {
                    if (newValue instanceof Boolean) {
                        Boolean boolVal = (Boolean) newValue;

                        SharedPreferences mPerferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
                        SharedPreferences.Editor mEditor = mPerferences.edit();
                        mEditor.putBoolean(CHECK_ISFORCE_KEY, boolVal);
                        mEditor.apply();

                    }
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
