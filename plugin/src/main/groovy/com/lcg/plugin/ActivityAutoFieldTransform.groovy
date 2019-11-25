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

        def none = true
        for (field in ctClass.declaredFields) {
            if (field.hasAnnotation("com.lcg.annotation.AutoField")) {
                none = false
                break
            }
        }
        //
        if (none) {
            if (saveCtMethod == null) {
                //  Activity  have not  saveInstanceState 
                saveCtMethod = CtNewMethod.make(generateActivitySaveMethod(), ctClass)
                ctClass.addMethod(saveCtMethod)
            } else {
                //  Activity have saveInstanceState 
                saveCtMethod.insertBefore("Bundle extras = getIntent().getExtras();\n" +
                        "if (extras!=null) \$1.putAll(extras);")
            }

            if (restoreCtMethod == null) {
                //  Activity  have not  onCreate 
                restoreCtMethod = CtNewMethod.make(generateActivityRestoreMethod(), ctClass)
                ctClass.addMethod(restoreCtMethod)
            } else {
                //  Activity have onCreate 
                restoreCtMethod.insertBefore("if (\$1 != null)  getIntent().putExtras(\$1);")
            }
        } else {
            if (saveCtMethod == null) {
                //  Activity  have not  saveInstanceState 
                saveCtMethod = CtNewMethod.make(generateActivitySaveMethod(
                        ctClass.name + Constant.GENERATED_FILE_SUFFIX), ctClass)
                ctClass.addMethod(saveCtMethod)
            } else {
                //  Activity have saveInstanceState 
                saveCtMethod.insertBefore(
                        "${ctClass.name}${Constant.GENERATED_FILE_SUFFIX}.onSaveInstanceState(this, \$1);")
            }

            if (restoreCtMethod == null) {
                //  Activity  have not  onCreate 
                restoreCtMethod = CtNewMethod.make(generateActivityRestoreMethod(
                        ctClass.name + Constant.GENERATED_FILE_SUFFIX), ctClass)
                ctClass.addMethod(restoreCtMethod)
            } else {
                //  Activity have onCreate 
                restoreCtMethod.insertBefore(
                        "if (\$1 != null) ${ctClass.name}${Constant.GENERATED_FILE_SUFFIX}.onRestoreInstanceState(this, \$1);"
                                + "else ${ctClass.name}${Constant.GENERATED_FILE_SUFFIX}.onInitState(this);")
            }
        }
    }

    static String generateActivityRestoreMethod(String delegatedName) {
        return "protected void onCreate(Bundle savedInstanceState) {\n" +
                "if (savedInstanceState != null) ${delegatedName}.onRestoreInstanceState(this, savedInstanceState);" + "\n" +
                "else ${delegatedName}.onInitState(this);" + "\n" +
                "super.onCreate(savedInstanceState);\n" +
                "}"
    }

    static String generateActivitySaveMethod(String delegatedName) {
        return "protected void onSaveInstanceState(Bundle outState) {\n" +
                "${delegatedName}.onSaveInstanceState(this, outState);" + "\n" +
                "super.onSaveInstanceState(outState);\n" +
                "}"
    }

    static String generateActivityRestoreMethod() {
        return "protected void onCreate(Bundle savedInstanceState) {\n" +
                "if (savedInstanceState != null) getIntent().putExtras(savedInstanceState);" + "\n" +
                "super.onCreate(savedInstanceState);\n" +
                "}"
    }

    static String generateActivitySaveMethod() {
        return "protected void onRestoreInstanceState(Bundle savedInstanceState) {\n" +
                "super.onRestoreInstanceState(savedInstanceState);\n" +
                "Bundle extras = getIntent().getExtras();\n" +
                "if (extras!=null)\n" +
                "savedInstanceState.putAll(extras);\n" +
                "}"
    }
}
