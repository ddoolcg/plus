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
                //  Fragment have not saveInstanceState
                saveCtMethod = CtNewMethod.make(generateFragmentSaveMethod(
                        ctClass.name + Constant.GENERATED_FILE_SUFFIX), ctClass)
                ctClass.addMethod(saveCtMethod)
            } else {
                //  Fragment have saveInstanceState
                saveCtMethod.insertBefore(
                        "if (getArguments() != null) \$1.putAll(getArguments());\n" +
                                "${ctClass.name}${Constant.GENERATED_FILE_SUFFIX}.onSaveInstanceState(this, \$1);")
            }

            if (restoreCtMethod == null) {
                // Fragment have not onCreateView
                restoreCtMethod = CtNewMethod.make(generateFragmentRestoreMethod(
                        ctClass.name + Constant.GENERATED_FILE_SUFFIX), ctClass)
                ctClass.addMethod(restoreCtMethod)
            } else {
                // Fragment have onCreateView
                restoreCtMethod.insertBefore("if (\$3 != null) ${ctClass.name}${Constant.GENERATED_FILE_SUFFIX}.onRestoreInstanceState(this, \$3);" +
                        "else if (getArguments() != null) ${ctClass.name}${Constant.GENERATED_FILE_SUFFIX}.onRestoreInstanceState(this, getArguments());")
            }
        }
    }

    static String generateFragmentRestoreMethod(String delegatedName) {
        return "public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {\n" +
                "if (savedInstanceState != null) ${delegatedName}.onRestoreInstanceState(this, savedInstanceState);" +
                "else if (getArguments() != null) ${delegatedName}.onRestoreInstanceState(this, getArguments());" +
                "return null;\n" +
                "}"
    }

    static String generateFragmentSaveMethod(String delegatedName) {
        return "public void onSaveInstanceState(Bundle outState) {\n" +
                "if (getArguments() != null) outState.putAll(getArguments());\n" +
                "${delegatedName}.onSaveInstanceState(this, outState);\n" +
                "super.onSaveInstanceState(outState);\n" +
                "}"
    }
}
