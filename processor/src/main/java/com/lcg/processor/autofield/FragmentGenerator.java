package com.lcg.processor.autofield;

import com.lcg.annotation.AutoField;
import com.lcg.processor.Constant;
import com.lcg.processor.Generator;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;

import static com.lcg.processor.Constant.BUNDLE_CLASS;

public class FragmentGenerator implements Generator {
    Element element;
    boolean isKotlinClass;
    ProcessingEnvironment processingEnv;

    public FragmentGenerator(ProcessingEnvironment processingEnv, boolean isKotlinClass, Element element) {
        this.element = element;
        this.isKotlinClass = isKotlinClass;
        this.processingEnv = processingEnv;
    }

    @Override
    public JavaFile createSourceFile() {
        String className = element.getSimpleName().toString();
        String packageName = ((PackageElement) element.getEnclosingElement()).getQualifiedName()
                .toString();

        List<? extends Element> enclosedElementsInside =
                element.getEnclosedElements();

        List<Element> autoStateFields = new ArrayList<>();

        for (Element testField : enclosedElementsInside) {
            if (testField.getKind() == ElementKind.FIELD && testField.getAnnotation(AutoField.class) != null) {
                autoStateFields.add(testField);
            }
        }

        if (autoStateFields.size() == 0) return null;

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className + Constant.ACTIVITY_AUTO_FIELD_EXTRAS)
                .addModifiers(Modifier.FINAL);
        classBuilder.addJavadoc("automatically use\n@author lei.chuguang Email:475825657@qq.com from pxjy");
        addMethodsAndFields(classBuilder, autoStateFields);
        // Create
        TypeSpec saveState = classBuilder.build();
        return JavaFile.builder(packageName, saveState).build();
    }


    protected void addMethodsAndFields(TypeSpec.Builder autoFieldClass, List<Element> autoFieldFields) {
        MethodSpec.Builder saveStateMethodBuilder = MethodSpec
                .methodBuilder("onSaveInstanceState")
                .addParameter(TypeName.get(element.asType()), "instance")
                .addParameter(BUNDLE_CLASS, "outState")
                .addModifiers(Modifier.STATIC);
        //
        for (Element field : autoFieldFields) {
            StateHelper.statementSaveValueIntoBundle(processingEnv, isKotlinClass, saveStateMethodBuilder,
                    field, "instance", "outState");
        }
        autoFieldClass.addMethod(saveStateMethodBuilder.build());
        //
        MethodSpec.Builder restoreStateMethodBuilder = MethodSpec
                .methodBuilder("onRestoreInstanceState")
                .addParameter(TypeName.get(element.asType()), "instance")
                .addParameter(BUNDLE_CLASS, "savedInstanceState")
                .addModifiers(Modifier.STATIC);

        for (Element field : autoFieldFields) {
            StateHelper.statementGetValueFromBundle(processingEnv, isKotlinClass, restoreStateMethodBuilder,
                    field, "instance", "savedInstanceState");
        }
        autoFieldClass.addMethod(restoreStateMethodBuilder.build());
    }

}
