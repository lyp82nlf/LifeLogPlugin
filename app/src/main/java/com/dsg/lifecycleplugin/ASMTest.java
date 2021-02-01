package com.dsg.lifecycleplugin;

import android.util.Log;

import androidx.multidex.BuildConfig;


/**
 * @author DSG
 * @Project LifeCyclePlugin
 * @date 2021/1/29
 * @describe
 */
public class ASMTest {
    public void test1() {
        Log.d("DSG", "current activity: " + getClass().getSimpleName());
    }

    public void test2() {
        if (BuildConfig.DEBUG) {

        }
    }
}