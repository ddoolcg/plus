package com.lcg.plugin

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Registers annotation processing and ASM instrumentation for Android variants.
 *
 * @author Lei Chuguang
 * @date 2026-08-13
 */
class AutoFieldPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        if (project.plugins.hasPlugin("com.android.application")
                || project.plugins.hasPlugin("com.android.library")) {
            def ver = "3.1"
            if (project.plugins.hasPlugin("kotlin-android")) {
                project.dependencies {
                    kapt "com.lcg.plugin:processor:$ver"
                    implementation "com.lcg.plugin:annotation:$ver"
                }
            } else {
                project.dependencies {
                    annotationProcessor "com.lcg.plugin:processor:$ver"
                    implementation "com.lcg.plugin:annotation:$ver"
                }
            }

            def androidComponents = project.extensions.findByType(AndroidComponentsExtension)
            if (androidComponents != null) {
                androidComponents.onVariants(androidComponents.selector().all()) { variant ->
                    variant.instrumentation.transformClassesWith(
                            AutoFieldClassVisitorFactory,
                            InstrumentationScope.PROJECT) { }
                    variant.instrumentation.setAsmFramesComputationMode(
                        FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS)
                }
            }
        }
    }
}
