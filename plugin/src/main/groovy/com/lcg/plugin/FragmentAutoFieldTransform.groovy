package com.lcg.plugin


import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.CtNewMethod
import org.gradle.api.Project

class FragmentAutoFieldTransform {

    CtClass ctClass
    ClassPool classPool
    Project project

    FragmentAutoFieldTransform(CtClass ctClass, ClassPool classPool, Project project) {
        this.ctClass = ctClass
        this.classPool = classPool
        this.project = project
    }

    void handleFragmentSaveState() {
        CtClass bundleCtClass = classPool.get("android.os.Bundle")
        CtClass inflaterCtClass = classPool.get("android.view.LayoutInflater")
        CtClass viewGroupCtClass = classPool.get("android.view.ViewGroup")

        CtMethod saveCtMethod = ctClass.declaredMethods.find {
            it.name == "onSaveInstanceState" && it.parameterTypes == [bundleCtClass] as CtClass[]
        }
        CtMethod restoreCtMethod = ctClass.declaredMethods.find {
            it.name == "onCreateView" && it.parameterTypes == [inflaterCtClass, viewGroupCtClass, bundleCtClass] as CtClass[]
        }

        def list = []
        ctClass.declaredFields.each { field ->
            if (field.hasAnnotation("com.lcg.annotation.AutoField")) {
                list.add(field)
            }
        }
        //
        if (list.size() > 0) {
            if (saveCtMethod == null) {
                // 原来的 Fragment 没有 saveInstanceState 方法
                saveCtMethod = CtNewMethod.make(generateFragmentSaveMethod(
                        ctClass.name + Constant.GENERATED_FILE_SUFFIX), ctClass)
                ctClass.addMethod(saveCtMethod)
            } else {
                // 原来的 Fragment 有 saveInstanceState 方法
                saveCtMethod.insertBefore(
                        "if (getArguments() != null) \$1.putAll(getArguments());\n" +
                                "${ctClass.name}${Constant.GENERATED_FILE_SUFFIX}.onSaveInstanceState(this, \$1);")
            }

            if (restoreCtMethod == null) {
                // 原来的 Fragment 没有 onCreateView 方法
                restoreCtMethod = CtNewMethod.make(generateFragmentRestoreMethod(
                        ctClass.name + Constant.GENERATED_FILE_SUFFIX), ctClass)
                ctClass.addMethod(restoreCtMethod)
            } else {
                // 原来的 Fragment 有 onCreateView 方法
                restoreCtMethod.insertBefore("if (\$3 != null) ${ctClass.name}${Constant.GENERATED_FILE_SUFFIX}.onRestoreInstanceState(this, \$3);" +
                        "else if (getArguments() != null) ${ctClass.name}${Constant.GENERATED_FILE_SUFFIX}.onRestoreInstanceState(this, getArguments());")
            }
        }
    }

    // Fragment onActivityCreated 不存在的情况下创建 onActivityCreated 方法
    static String generateFragmentRestoreMethod(String delegatedName) {
        return "public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {\n" +
                "if (savedInstanceState != null) ${delegatedName}.onRestoreInstanceState(this, savedInstanceState);" +
                "else if (getArguments() != null) ${delegatedName}.onRestoreInstanceState(this, getArguments());" +
                "return null;\n" +
                "}"
    }

    // Fragment onSaveInstanceState 不存在的情况下创建 onSaveInstanceState
    static String generateFragmentSaveMethod(String delegatedName) {
        return "public void onSaveInstanceState(Bundle outState) {\n" +
                "if (getArguments() != null) outState.putAll(getArguments());\n" +
                "${delegatedName}.onSaveInstanceState(this, outState);\n" +
                "super.onSaveInstanceState(outState);\n" +
                "}"
    }
}
