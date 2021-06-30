package com.lcg.processor.autofield;

import com.lcg.processor.Constant;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import static com.lcg.processor.Constant.BUNDLE_CLASS;

public class ActivityGenerator extends FragmentGenerator {
    public ActivityGenerator(ProcessingEnvironment processingEnv, boolean isKotlinClass, Element element) {
        super(processingEnv, isKotlinClass, element);
    }

    @Override
    protected void addMethodsAndFields(TypeSpec.Builder autoFieldClass, List<Element> autoFieldFields) {
        MethodSpec.Builder saveStateMethodBuilder = MethodSpec
                .methodBuilder("onSaveInstanceState")
                .addParameter(TypeName.get(element.asType()), "instance")
                .addParameter(BUNDLE_CLASS, "outState")
                .addModifiers(Modifier.STATIC);
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
        restoreStateMethodBuilder.addStatement("$T intent = instance.getIntent()", Constant.INTENT_CLASS);
        for (Element field : autoFieldFields) {
            StateHelper.statementGetValueFromBundle(processingEnv, isKotlinClass, restoreStateMethodBuilder,
                    field, "instance", "savedInstanceState");
            StateHelper.statementSaveValueIntoIntent(isKotlinClass, restoreStateMethodBuilder, field, "instance", "intent");
        }
        autoFieldClass.addMethod(restoreStateMethodBuilder.build());
        //
        MethodSpec.Builder initMethodBuilder = MethodSpec
                .methodBuilder("onInitState")
                .addParameter(TypeName.get(element.asType()), "instance")
                .addModifiers(Modifier.STATIC);
        initMethodBuilder.addStatement("Bundle extras = instance.getIntent().getExtras()");
        initMethodBuilder.beginControlFlow("if (extras!=null)");
        for (Element field : autoFieldFields) {
            StateHelper.statementGetValueFromBundle(processingEnv, isKotlinClass, initMethodBuilder,
                    field, "instance", "extras");
        }
        initMethodBuilder.endControlFlow();
        autoFieldClass.addMethod(initMethodBuilder.build());
    }

}
