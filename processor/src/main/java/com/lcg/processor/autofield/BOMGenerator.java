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

public class BOMGenerator implements Generator {
    private Element element;
    private boolean isKotlinClass;
    private ProcessingEnvironment processingEnv;

    public BOMGenerator(ProcessingEnvironment processingEnv, boolean isKotlinClass, Element element) {
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

    private void addMethodsAndFields(TypeSpec.Builder autoFieldClass, List<Element> autoFieldFields) {
        MethodSpec.Builder initMethodBuilder = MethodSpec
                .methodBuilder("onInitState")
                .addParameter(TypeName.get(element.asType()), "instance")
                .addModifiers(Modifier.STATIC);
        initMethodBuilder.addStatement("$T activity = instance.getActivity()", Constant.ACTIVITY_CLASS);
        initMethodBuilder.beginControlFlow("if (activity!=null)");
        initMethodBuilder.addStatement("$T extras = activity.getIntent().getExtras()", Constant.BUNDLE_CLASS);
        initMethodBuilder.beginControlFlow("if (extras!=null)");
        for (Element field : autoFieldFields) {
            StateHelper.statementGetValueFromBundle(processingEnv, isKotlinClass, initMethodBuilder,
                    field, "instance", "extras");
        }
        initMethodBuilder.endControlFlow();
        initMethodBuilder.endControlFlow();
        autoFieldClass.addMethod(initMethodBuilder.build());
    }

}
