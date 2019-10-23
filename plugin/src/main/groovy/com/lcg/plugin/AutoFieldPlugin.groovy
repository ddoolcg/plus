package com.lcg.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class AutoFieldPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        if (project.plugins.hasPlugin("com.android.application")
                || project.plugins.hasPlugin("com.android.library")) {
            if (project.plugins.hasPlugin("kotlin-android")) {
                project.dependencies {
                    kapt "com.pxjy.plugin:processor:1.1"
                    implementation "com.pxjy.plugin:annotation:1.1"
                }
            } else {
                project.dependencies {
                    annotationProcessor "com.pxjy.plugin:processor:1.1"
                    implementation "com.pxjy.plugin:annotation:1.1"
                }
            }

            if (project.plugins.hasPlugin("com.android.application")) {
                project.android.registerTransform(new AutoFieldTransform(project))
            }
        }
    }
}
