package com.lcg.processor;

import com.google.auto.service.AutoService;
import com.lcg.annotation.AutoField;
import com.lcg.annotation.Route;
import com.lcg.processor.autofield.ActivityGenerator;
import com.lcg.processor.autofield.BOMGenerator;
import com.lcg.processor.autofield.BundleGenerator;
import com.lcg.processor.autofield.FragmentGenerator;
import com.lcg.processor.autofield.IntentGenerator;
import com.lcg.processor.route.RouteGenerator;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;


@AutoService(Processor.class)
@SupportedAnnotationTypes({"com.lcg.annotation.AutoField", "com.lcg.annotation.Route"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class StateProcessor extends AbstractProcessor {

    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //SupportedAnnotationTypes 无效设置  因为set没有使用
        autoFieldHandle(roundEnvironment);
        routeHandle(roundEnvironment);
        return true;
    }

    /**
     * AutoField处理
     */
    private void autoFieldHandle(RoundEnvironment roundEnvironment) {
        Set<? extends Element> autoStateClasses = roundEnvironment
                .getElementsAnnotatedWith(AutoField.class);
        Set<Element> stateClasses = new HashSet<>();
        if (autoStateClasses != null) {
            for (Element element : autoStateClasses) {
                TypeElement te = (TypeElement) element.getEnclosingElement();
                if (!stateClasses.contains(te)) {
                    stateClasses.add(te);
                }
            }
        }

        for (Element element : stateClasses) {
            if (element.getKind() != ElementKind.CLASS) {
                continue;
            }

            boolean isKotlinClass = false;
            List<? extends AnnotationMirror> list = processingEnv.getElementUtils()
                    .getAllAnnotationMirrors(element);
            for (int i = 0; list != null && i < list.size(); i++) {
                if ("kotlin.Metadata".equals(((TypeElement) list.get(i).getAnnotationType().asElement()).getQualifiedName().toString())) {
                    isKotlinClass = true;
                    break;
                }
            }

            Generator generator;
            Generator generator1 = null;
            if (checkIsSubClassOf(element, Constant.CLASS_ACTIVITY, Constant.CLASS_FRAGMENT_ACTIVITY)) {
                generator = new ActivityGenerator(processingEnv, isKotlinClass, element);
                generator1 = new IntentGenerator(element);
            } else if (checkIsSubClassOf(element, Constant.CLASS_V4_FRAGMENT, Constant.CLASS_FRAGMENT)) {
                generator = new FragmentGenerator(processingEnv, isKotlinClass, element);
                generator1 = new BundleGenerator(processingEnv, element);
            } else if (checkIsSubClassOf(element, Constant.CLASS_BASE_OBSERVABLE_ME)) {
                generator = new BOMGenerator(processingEnv, isKotlinClass, element);
            } else {
                continue;
            }

            JavaFile javaFile = generator.createSourceFile();
            JavaFile javaFile1 = null;
            if (generator1 != null) {
                javaFile1 = generator1.createSourceFile();
            }
            // Finally, write the source to file
            try {
                if (javaFile != null) {
                    javaFile.writeTo(processingEnv.getFiler());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (javaFile1 != null) {
                    javaFile1.writeTo(processingEnv.getFiler());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkIsSubClassOf(Element element, String... superClasses) {
        Elements elementUtils = processingEnv.getElementUtils();
        Types typeUtils = processingEnv.getTypeUtils();
        for (String clazz : superClasses) {
            try {
                boolean isSubType = typeUtils.isSubtype(
                        element.asType(),
                        elementUtils.getTypeElement(clazz).asType()
                );
                if (isSubType) return true;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Route处理
     */
    private void routeHandle(RoundEnvironment roundEnvironment) {
        Set<? extends Element> routeClass = roundEnvironment.getElementsAnnotatedWith(Route.class);
        Set<Element> classes = new HashSet<>();
        if (routeClass != null) {
            for (Element element : routeClass) {
                TypeElement te = (TypeElement) element.getEnclosingElement();
                if (!classes.contains(te) && te.getKind() == ElementKind.CLASS) {
                    classes.add(te);
                }
            }
        }
        //
        if (!classes.isEmpty()) {
            RouteGenerator generator = new RouteGenerator(processingEnv);
            for (Element element : classes) {
                generator.addElement(element);
            }
            //
            JavaFile javaFile = generator.createSourceFile();
            try {
                if (javaFile != null) {
                    javaFile.writeTo(processingEnv.getFiler());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
