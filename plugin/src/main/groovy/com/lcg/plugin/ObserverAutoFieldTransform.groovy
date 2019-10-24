package com.lcg.plugin

import javassist.ClassPool
import javassist.CtClass
import org.gradle.api.Project

class ObserverAutoFieldTransform {

    CtClass ctClass
    ClassPool classPool
    Project project

    ObserverAutoFieldTransform(CtClass ctClass, ClassPool classPool, Project project) {
        this.ctClass = ctClass
        this.classPool = classPool
        this.project = project
    }

    void handleFragmentSaveState() {
        def create = false
        for (field in ctClass.declaredFields) {
            if (field.hasAnnotation("com.lcg.annotation.AutoField")) {
                create = true
                break
            }
        }
        //
        if (create) {
            ctClass.constructors.each { initCtMethod ->
                if (initCtMethod.callsSuper())
                    initCtMethod.insertBeforeBody(
                            "${ctClass.name}${Constant.GENERATED_FILE_SUFFIX}.onInitState(this);")
            }
        }
    }
}
