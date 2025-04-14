package com.example.healthcareapp;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

public class MyApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        // Áp dụng ngôn ngữ đã lưu
        super.attachBaseContext(LocaleHelper.setLocale(base, LocaleHelper.getLanguage(base)));
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleHelper.setLocale(this, LocaleHelper.getLanguage(this));
    }
}