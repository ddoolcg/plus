package com.lcg.processor;

import com.lcg.annotation.AutoField;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;

public class BundleGenerator implements Generator {
    ProcessingEnvironment processingEnv;
    private Element element;
    private String className;
    private String packageName;

    BundleGenerator(ProcessingEnvironment processingEnv, Element element) {
        this.element = element;
        className = element.getSimpleName().toString() + Constant.FRAGMENT_BUNDLE;
        packageName = ((PackageElement) element.getEnclosingElement()).getQualifiedName()
                .toString();
        this.processingEnv = processingEnv;
    }

    @Override
    public JavaFile createSourceFile() {
        List<? extends Element> enclosedElementsInside =
                element.getEnclosedElements();

        List<Element> autoStateFields = new ArrayList<>();

        for (Element testField : enclosedElementsInside) {
            if (testField.getKind() == ElementKind.FIELD && testField.getAnnotation(AutoField.class) != null) {
                autoStateFields.add(testField);
            }
        }

        if (autoStateFields.size() == 0) return null;

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC);
        classBuilder.addJavadoc(element.getSimpleName().toString() + " bundle builder\n@author lei.chuguang Email:475825657@qq.com from pxjy");
        addMethodsAndFields(classBuilder, autoStateFields);
        // Create
        TypeSpec saveState = classBuilder.build();
        return JavaFile.builder(packageName, saveState).build();
    }


    private void addMethodsAndFields(TypeSpec.Builder intentClass, List<Element> autoFieldFields) {
        //build
        MethodSpec.Builder createMethodBuilder = MethodSpec
                .methodBuilder("build")
                .returns(Constant.BUNDLE_CLASS)
                .addModifiers(Modifier.PUBLIC);
        createMethodBuilder.addStatement("Bundle bundle = new Bundle()");
        for (Element field : autoFieldFields) {
            //field
            String pName = field.getSimpleName().toString();
            TypeName typeName = TypeName.get(field.asType());
            intentClass.addField(typeName, pName, Modifier.PRIVATE);
            //set
            ParameterSpec parameter = ParameterSpec.builder(typeName, pName)
                    .addAnnotation(Constant.NONNULL_CLASS).build();
            MethodSpec.Builder setMethod = MethodSpec
                    .methodBuilder("set" + upperFirstChar(pName))
                    .returns(ClassName.get(packageName, className))
                    .addParameter(parameter)
                    .addModifiers(Modifier.PUBLIC);
            setMethod.addStatement("this.$N = $N", pName, pName);
            setMethod.addStatement("return this");
            intentClass.addMethod(setMethod.build());
            StateHelper.statementSaveValueIntoBundle(processingEnv, createMethodBuilder, field, "bundle");
        }
        createMethodBuilder.addStatement("return bundle");
        intentClass.addMethod(createMethodBuilder.build());
    }

    private static String upperFirstChar(String str) {
        char[] ch = str.toCharArray();
        if (ch[0] >= 'a' && ch[0] <= 'z') {
            ch[0] = (char) (ch[0] - 32);
        }
        return new String(ch);
    }
}
