package com.lcg.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class AutoFieldPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        if (project.plugins.hasPlugin("com.android.application")
                || project.plugins.hasPlugin("com.android.library")) {

            project.dependencies {
                kapt "com.pxjy.plugin:processor:1.0"
            }

            if (project.plugins.hasPlugin("com.android.application")) {
                project.android.registerTransform(new AutoFieldTransform(project))
            }
        }
    }
}
