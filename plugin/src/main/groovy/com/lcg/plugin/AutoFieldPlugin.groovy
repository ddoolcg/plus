package com.lcg.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class AutoFieldPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        if (project.plugins.hasPlugin("com.android.application")
                || project.plugins.hasPlugin("com.android.library")) {
            def ver = "2.6"
            if (project.plugins.hasPlugin("kotlin-android")) {
                project.dependencies {
                    kapt "com.pxjy.plugin:processor:$ver"
                    implementation "com.pxjy.plugin:annotation:$ver"
                }
            } else {
                project.dependencies {
                    annotationProcessor "com.pxjy.plugin:processor:$ver"
                    implementation "com.pxjy.plugin:annotation:$ver"
                }
            }

            if (project.plugins.hasPlugin("com.android.application")) {
                project.android.registerTransform(new AutoFieldTransform(project))
            }
        }
    }
}
