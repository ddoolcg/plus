package com.lcg.plugin


import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.CtNewMethod
import org.gradle.api.Project

class ActivityAutoFieldTransform {
    CtClass ctClass
    ClassPool classPool
    Project project

    ActivityAutoFieldTransform(Project project, CtClass ctClass, ClassPool classPool) {
        this.ctClass = ctClass
        this.classPool = classPool
        this.project = project
    }

    void handleActivitySaveState() {
        CtClass bundleCtClass = classPool.get("android.os.Bundle")
        CtMethod saveCtMethod = ctClass.declaredMethods.find {
            it.name == "onSaveInstanceState" && it.parameterTypes == [bundleCtClass] as CtClass[]
        }
        CtMethod restoreCtMethod = ctClass.declaredMethods.find {
            it.name == "onCreate" && it.parameterTypes == [bundleCtClass] as CtClass[]
        }

        def list = []
        ctClass.declaredFields.each { field ->
            if (field.hasAnnotation("com.lcg.annotation.AutoField")) {
                list.add(field)
            }
        }
        if (list.size() == 0) {
            if (saveCtMethod == null) {
                // 原来的 Activity 没有 saveInstanceState 方法
                saveCtMethod = CtNewMethod.make(generateActivitySaveMethod(), ctClass)
                ctClass.addMethod(saveCtMethod)
            } else {
                // 原来的 Activity 有 saveInstanceState 方法
                saveCtMethod.insertBefore("\$1.putAll(getIntent().getExtras());")
            }

            if (restoreCtMethod == null) {
                // 原来的 Activity 没有 onCreate 方法
                restoreCtMethod = CtNewMethod.make(generateActivityRestoreMethod(), ctClass)
                ctClass.addMethod(restoreCtMethod)
            } else {
                // 原来的 Activity 有 onCreate 方法
                restoreCtMethod.insertBefore("if (\$1 != null)  getIntent().putExtras(\$1);")
            }
        } else {
            if (saveCtMethod == null) {
                // 原来的 Activity 没有 saveInstanceState 方法
                saveCtMethod = CtNewMethod.make(generateActivitySaveMethod(
                        ctClass.name + Constant.GENERATED_FILE_SUFFIX), ctClass)
                ctClass.addMethod(saveCtMethod)
            } else {
                // 原来的 Activity 有 saveInstanceState 方法
                saveCtMethod.insertBefore(
                        "${ctClass.name}${Constant.GENERATED_FILE_SUFFIX}.onSaveInstanceState(this, \$1);")
            }

            if (restoreCtMethod == null) {
                // 原来的 Activity 没有 onCreate 方法
                restoreCtMethod = CtNewMethod.make(generateActivityRestoreMethod(
                        ctClass.name + Constant.GENERATED_FILE_SUFFIX), ctClass)
                ctClass.addMethod(restoreCtMethod)
            } else {
                // 原来的 Activity 有 onCreate 方法
                restoreCtMethod.insertBefore(
                        "if (\$1 != null) ${ctClass.name}${Constant.GENERATED_FILE_SUFFIX}.onRestoreInstanceState(this, \$1);"
                                + "else ${ctClass.name}${Constant.GENERATED_FILE_SUFFIX}.onInitState(this);")
            }
        }
    }

    // Activity onCreate 不存在的情况下创建 onCreate 方法
    static String generateActivityRestoreMethod(String delegatedName) {
        return "protected void onCreate(Bundle savedInstanceState) {\n" +
                "if (savedInstanceState != null) ${delegatedName}.onRestoreInstanceState(this, savedInstanceState);" + "\n" +
                "else ${delegatedName}.onInitState(this);" + "\n" +
                "super.onCreate(savedInstanceState);\n" +
                "}"
    }

    // Activity onSaveInstanceState 不存在的情况下创建 onSaveInstanceState
    static String generateActivitySaveMethod(String delegatedName) {
        return "protected void onSaveInstanceState(Bundle outState) {\n" +
                "${delegatedName}.onSaveInstanceState(this, outState);" + "\n" +
                "super.onSaveInstanceState(outState);\n" +
                "}"
    }
    //无注解 Activity onCreate 不存在的情况下创建 onCreate 方法
    static String generateActivityRestoreMethod() {
        return "protected void onCreate(Bundle savedInstanceState) {\n" +
                "if (savedInstanceState != null) getIntent().putExtras(savedInstanceState);" + "\n" +
                "super.onCreate(savedInstanceState);\n" +
                "}"
    }

    //无注解 Activity onSaveInstanceState 不存在的情况下创建 onSaveInstanceState
    static String generateActivitySaveMethod() {
        return "protected void onSaveInstanceState(Bundle outState) {\n" +
                "super.onSaveInstanceState(outState);\n" +
                "outState.putAll(getIntent().getExtras());\n" +
                "}"
    }
}
