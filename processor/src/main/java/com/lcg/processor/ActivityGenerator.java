package com.lcg.processor;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

public class ActivityGenerator extends FragmentGenerator {
    ActivityGenerator(ProcessingEnvironment processingEnv, boolean isKotlinClass, Element element) {
        super(processingEnv, isKotlinClass, element);
    }

    @Override
    protected void addMethodsAndFields(TypeSpec.Builder autoFieldClass, List<Element> autoFieldFields) {
        super.addMethodsAndFields(autoFieldClass, autoFieldFields);
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
