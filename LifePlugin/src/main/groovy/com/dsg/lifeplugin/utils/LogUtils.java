package com.dsg.lifeplugin.utils;

/**
 * @author DSG
 * @Project LifeCyclePlugin
 * @date 2021/1/31
 * @describe
 */
public class LogUtils {
    public static void log(String s) {
        if (EnvUtils.getInstance().isDev()) {
            System.out.println(s);
        }
    }
}