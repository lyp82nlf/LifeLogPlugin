package com.dsg.lifecycleplugin;

import android.app.Application;

import androidx.multidex.MultiDex;

/**
 * @author DSG
 * @Project LifeCyclePlugin
 * @date 2021/1/31
 * @describe
 */
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
    }
}