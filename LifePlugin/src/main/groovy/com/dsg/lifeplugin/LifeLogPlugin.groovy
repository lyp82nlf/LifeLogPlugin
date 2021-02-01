package com.dsg.lifeplugin

import com.dsg.lifeplugin.utils.EnvUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger

class LifeLogPlugin implements Plugin<Project> {
    static Logger logger

    @Override
    void apply(Project project) {
        logger = project.logger
        logger.quiet "================LifeLogPlugin start================"
        EnvUtils.getInstance().init(project)
        project.android.registerTransform(new LifeLogTransform())
    }
}