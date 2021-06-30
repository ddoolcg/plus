package com.lcg.processor.route;

import com.lcg.annotation.Route;
import com.lcg.processor.Constant;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

public class RouteGenerator {
    private TypeMirror typeBaseActivity;
    private Types typeUtils;
    private MethodSpec.Builder methodBuilder;
    private HashMap<String, ArrayList<ExecutableElement>> map = new HashMap<>();

    public RouteGenerator(ProcessingEnvironment processingEnv) {
        typeBaseActivity = processingEnv.getElementUtils().getTypeElement(Constant.CLASS_ACTIVITY).asType();
        typeUtils = processingEnv.getTypeUtils();
        //
        methodBuilder = MethodSpec
                .methodBuilder("start")
                .addParameter(Constant.BASE_ACTIVITY_CLASS, "activity")
                .addParameter(Constant.URI_CLASS, "uri")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.BOOLEAN);
        methodBuilder.addStatement("String path = uri.getPath()");
        methodBuilder.addStatement("$T<String> names = uri.getQueryParameterNames()", Constant.SET_CLASS);
    }

    public void addElement(Element element) {
        List<? extends Element> enclosedElementsInside =
                element.getEnclosedElements();
        for (Element methodElement : enclosedElementsInside) {
            if (methodElement.getKind() == ElementKind.METHOD
                    && methodElement.getAnnotation(Route.class) != null
                    && methodElement.getModifiers().contains(Modifier.PUBLIC)) {
                //
                ExecutableElement executableElement = (ExecutableElement) methodElement;
                List<? extends VariableElement> parameters = executableElement.getParameters();
                boolean isSubType = false;
                for (VariableElement parameter : parameters) {
                    try {
                        isSubType = typeUtils.isSubtype(parameter.asType(), typeBaseActivity);
                        if (isSubType) break;
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
                //
                if (isSubType) {
                    String path = executableElement.getAnnotation(Route.class).value();
                    if (map.containsKey(path)) {
                        ArrayList<ExecutableElement> list = map.get(path);
                        int pSize = parameters.size();
                        int size = list.size();
                        int index = size;
                        for (int i = 0; i < size; i++) {
                            int lastSize = list.get(i).getParameters().size();
                            if (pSize > lastSize) {
                                index = i;
                                break;
                            }
                        }
                        list.add(index, executableElement);
                    } else {
                        ArrayList<ExecutableElement> list = new ArrayList<>();
                        list.add(executableElement);
                        map.put(path, list);
                    }
                }
            }
        }
    }

    public JavaFile createSourceFile() {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder("Route")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        classBuilder.addJavadoc("Uri Route\n@author lei.chuguang Email:475825657@qq.com from pxjy");
        addMethodContent();
        methodBuilder.addStatement("return false");
        classBuilder.addMethod(methodBuilder.build());
        // Create
        TypeSpec saveState = classBuilder.build();
        return JavaFile.builder("com.lcg.processor", saveState).build();
    }

    private void addMethodContent() {
        Set<Map.Entry<String, ArrayList<ExecutableElement>>> entries = map.entrySet();
        for (Map.Entry<String, ArrayList<ExecutableElement>> entry : entries) {
            methodBuilder.beginControlFlow("if(\"" + entry.getKey() + "\".equals(path))");
            methodBuilder.addStatement("$T<String> strings", Constant.LIST_CLASS);
            //
            ArrayList<ExecutableElement> value = entry.getValue();
            for (ExecutableElement method : value) {
                List<? extends VariableElement> parameters = method.getParameters();
                StringBuilder sb = new StringBuilder();
                boolean haveParameters = parameters.size() > 1;
                //
                for (VariableElement parameter : parameters) {
                    if (typeUtils.isSubtype(parameter.asType(), typeBaseActivity)) {
                        continue;
                    }
                    //
                    String name = parameter.getSimpleName().toString();
                    sb.append(",\"").append(name).append("\"");
                }
                if (haveParameters) {
                    methodBuilder.addStatement("strings = $T.asList(" + sb.substring(1) + ")", Constant.ARRAYS_CLASS);
                    methodBuilder.beginControlFlow("if(names.containsAll(strings))");
                }
                //
                sb.delete(0, sb.length());
                for (VariableElement parameter : parameters) {
                    if (typeUtils.isSubtype(parameter.asType(), typeBaseActivity)) {
                        sb.append(",activity");
                        continue;
                    }
                    //
                    String name = parameter.getSimpleName().toString();
                    methodBuilder.addStatement("String " + name + " = uri.getQueryParameter(\"" + name + "\")");
                    //
                    String changeString;
                    switch (parameter.asType().toString()) {
                        case "int":
                        case "java.lang.Integer":
                            changeString = "Integer.parseInt(" + name + ")";
                            break;
                        case "long":
                        case "java.lang.Long":
                            changeString = "Long.parseLong(" + name + ")";
                            break;
                        case "char":
                        case "java.lang.Character":
                            changeString = "Character.parseCharacter(" + name + ")";
                            break;
                        case "short":
                        case "java.lang.Short":
                            changeString = "Short.parseShort(" + name + ")";
                            break;
                        case "byte":
                        case "java.lang.Byte":
                            changeString = "Byte.parseByte(" + name + ")";
                            break;
                        case "float":
                        case "java.lang.Float":
                            changeString = "Float.parseFloat(" + name + ")";
                            break;
                        case "double":
                        case "java.lang.Double":
                            changeString = "Double.parseDouble(" + name + ")";
                            break;
                        case "boolean":
                        case "java.lang.Boolean":
                            changeString = "Boolean.parseBoolean(" + name + ")";
                            break;
                        default:
                            changeString = name;
                            break;
                    }
                    sb.append(",").append(changeString);
                }
                //
                Element element = method.getEnclosingElement();
                methodBuilder.addStatement(TypeName.get(element.asType()) + "." + method.getSimpleName().toString() + "(" + sb.substring(1) + ")");
                //
                methodBuilder.addStatement("return true");
                if (haveParameters) methodBuilder.endControlFlow();
            }
            methodBuilder.endControlFlow();
        }
    }
}
