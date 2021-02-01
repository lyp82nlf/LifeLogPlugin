package com.dsg.lifeplugin.utils;

import org.gradle.api.Project;

/**
 * @author DSG
 * @Project LifeCyclePlugin
 * @date 2021/1/31
 * @describe
 */
public class EnvUtils {

    private Project project;

    private static volatile EnvUtils sInstance;

    public void init(Project pro) {
        project = pro;
    }

    public static EnvUtils getInstance() {
        if (sInstance == null) {
            synchronized (EnvUtils.class) {
                if (sInstance == null) {
                    sInstance = new EnvUtils();
                }
            }
        }
        return sInstance;
    }

    public boolean isDebug() {
        return project.getGradle().getStartParameter().getTaskNames().get(0).equals("assembleDebug");
    }

    public boolean isDev() {
        return true;
    }
}