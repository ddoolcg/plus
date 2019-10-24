package com.lcg.processor.autofield;

import com.lcg.annotation.AutoField;
import com.squareup.javapoet.MethodSpec;

import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

class StateHelper {
    static void statementSaveValueIntoIntent(MethodSpec.Builder methodBuilder, Element element,
                                             String intent) {
        String varName = element.getSimpleName().toString();
        String name = element.getAnnotation(AutoField.class).value();
        if (name.isEmpty()) name = varName;
        //
        String statement = String.format("%s.putExtra(%s, %s)", intent, "\"" + name + "\"", varName);

        methodBuilder.addStatement(statement);
    }

    static void statementSaveValueIntoBundle(ProcessingEnvironment processingEnv, MethodSpec.Builder methodBuilder, Element element,
                                             String bundleName) {
        String varName = element.getSimpleName().toString();
        String name = element.getAnnotation(AutoField.class).value();
        if (name.isEmpty()) name = varName;
        //
        String statement = null;
        switch (element.asType().toString()) {
            case "int":
                statement = String.format("%s.putInt(%s, %s)", bundleName, "\"" + name + "\"", varName);
                break;
            case "long":
                statement = String.format("%s.putLong(%s, %s)", bundleName, "\"" + name + "\"", varName);
                break;
            case "char":
                statement = String.format("%s.putChar(%s, %s)", bundleName, "\"" + name + "\"", varName);
                break;
            case "short":
                statement = String.format("%s.putShort(%s, %s)", bundleName, "\"" + name + "\"", varName);
                break;
            case "byte":
                statement = String.format("%s.putByte(%s, %s)", bundleName, "\"" + name + "\"", varName);
                break;
            case "float":
                statement = String.format("%s.putFloat(%s, %s)", bundleName, "\"" + name + "\"", varName);
                break;
            case "double":
                statement = String.format("%s.putDouble(%s, %s)", bundleName, "\"" + name + "\"", varName);
                break;
            case "boolean":
                statement = String.format("%s.putBoolean(%s, %s)", bundleName, "\"" + name + "\"", varName);
                break;
            case "java.lang.Integer":
            case "java.lang.Long":
            case "java.lang.Character":
            case "java.lang.Short":
            case "java.lang.Byte":
            case "java.lang.Float":
            case "java.lang.Double":
            case "java.lang.Boolean":
            case "java.io.Serializable":
                statement = String.format("%s.putSerializable(%s, %s)", bundleName, "\"" + name + "\"", varName);
                break;
            case "java.lang.String":
                statement = String.format("%s.putString(%s, %s)", bundleName, "\"" + name + "\"", varName);
                break;
            case "android.os.IBinder":
                statement = String.format("%s.putBinder(%s, %s)", bundleName, "\"" + name + "\"", varName);
                break;
            case "android.os.Bundle":
                statement = String.format("%s.putBundle(%s, %s)", bundleName, "\"" + name + "\"", varName);
                break;
            case "java.lang.CharSequence":
                statement = String.format("%s.putCharSequence(%s, %s)", bundleName, "\"" + name + "\"", varName);
                break;
            case "android.os.Parcelable":
                statement = String.format("%s.putParcelable(%s, %s)", bundleName, "\"" + name + "\"", varName);
                break;
            case "android.util.Size":
                statement = String.format("%s.putSize(%s, %s)", bundleName, "\"" + name + "\"", varName);
                break;
            case "android.util.SizeF":
                statement = String.format("%s.putSizeF(%s, %s)", bundleName, "\"" + name + "\"", varName);
                break;
            case "android.os.Parcelable[]":
                statement =
                        String.format("%s.putParcelableArray(%s, %s)", bundleName, "\"" + name + "\"", varName);
                break;
            case "byte[]":
                statement = String.format("%s.putByteArray(%s, %s)", bundleName, "\"" + name + "\"", varName);
                break;
            case "short[]":
                statement = String.format("%s.putShortArray(%s, %s)", bundleName, "\"" + name + "\"", varName);
                break;
            case "char[]":
                statement = String.format("%s.putCharArray(%s, %s)", bundleName, "\"" + name + "\"", varName);
                break;
            case "float[]":
                statement = String.format("%s.putFloatArray(%s, %s)", bundleName, "\"" + name + "\"", varName);
                break;
            case "java.lang.CharSequence[]":
                statement =
                        String.format("%s.putCharSequenceArray(%s, %s)", bundleName, "\"" + name + "\"", varName);
                break;
        }

        if (statement == null) {
            Types typeUtil = processingEnv.getTypeUtils();
            Elements elementUtil = processingEnv.getElementUtils();

            if (typeUtil.isSubtype(element.asType(),
                    elementUtil.getTypeElement("android.os.Parcelable").asType())) {
                statement = String.format("%s.putParcelable(%s, %s)", bundleName, "\"" + name + "\"", varName);
            } else if (typeUtil.isSubtype(element.asType(),
                    elementUtil.getTypeElement("java.io.Serializable").asType())) {
                statement = String.format("%s.putSerializable(%s, %s)", bundleName, "\"" + name + "\"", varName);
            }
        }

        if (statement != null) {
            methodBuilder.addStatement(statement);
        }
    }

    static void statementSaveValueIntoBundle(ProcessingEnvironment processingEnv,
                                             boolean isKotlinClass, MethodSpec.Builder methodBuilder, Element element,
                                             String instance, String bundleName) {
        String statement = null;
        String varName = element.getSimpleName().toString();
        String name = element.getAnnotation(AutoField.class).value();
        if (name.isEmpty()) name = varName;
        //
        boolean isKotlinField = isKotlinClass && isKotlinField(element);

        switch (element.asType().toString()) {
            case "int":
                statement = String.format("%s.putInt(%s, %s)", bundleName, "\"" + name + "\"",
                        getStatement(isKotlinField, instance, varName));
                break;
            case "long":
                statement = String.format("%s.putLong(%s, %s)", bundleName, "\"" + name + "\"",
                        getStatement(isKotlinField, instance, varName));
                break;
            case "char":
                statement = String.format("%s.putChar(%s, %s)", bundleName, "\"" + name + "\"",
                        getStatement(isKotlinField, instance, varName));
                break;
            case "short":
                statement = String.format("%s.putShort(%s, %s)", bundleName, "\"" + name + "\"",
                        getStatement(isKotlinField, instance, varName));
                break;
            case "byte":
                statement = String.format("%s.putByte(%s, %s)", bundleName, "\"" + name + "\"",
                        getStatement(isKotlinField, instance, varName));
                break;
            case "float":
                statement = String.format("%s.putFloat(%s, %s)", bundleName, "\"" + name + "\"",
                        getStatement(isKotlinField, instance, varName));
                break;
            case "double":
                statement = String.format("%s.putDouble(%s, %s)", bundleName, "\"" + name + "\"",
                        getStatement(isKotlinField, instance, varName));
                break;
            case "boolean":
                statement = String.format("%s.putBoolean(%s, %s)", bundleName, "\"" + name + "\"",
                        getStatement(isKotlinField, instance, varName));
                break;
            case "java.lang.Integer":
            case "java.lang.Long":
            case "java.lang.Character":
            case "java.lang.Short":
            case "java.lang.Byte":
            case "java.lang.Float":
            case "java.lang.Double":
            case "java.lang.Boolean":
            case "java.io.Serializable":
                statement = String.format("%s.putSerializable(%s, %s)", bundleName, "\"" + name + "\"",
                        getStatement(isKotlinField, instance, varName));
                break;
            case "java.lang.String":
                statement = String.format("%s.putString(%s, %s)", bundleName, "\"" + name + "\"",
                        getStatement(isKotlinField, instance, varName));
                break;
            case "android.os.IBinder":
                statement = String.format("%s.putBinder(%s, %s)", bundleName, "\"" + name + "\"",
                        getStatement(isKotlinField, instance, varName));
                break;
            case "android.os.Bundle":
                statement = String.format("%s.putBundle(%s, %s)", bundleName, "\"" + name + "\"",
                        getStatement(isKotlinField, instance, varName));
                break;
            case "java.lang.CharSequence":
                statement = String.format("%s.putCharSequence(%s, %s)", bundleName, "\"" + name + "\"",
                        getStatement(isKotlinField, instance, varName));
                break;
            case "android.os.Parcelable":
                statement = String.format("%s.putParcelable(%s, %s)", bundleName, "\"" + name + "\"",
                        getStatement(isKotlinField, instance, varName));
                break;
            case "android.util.Size":
                statement = String.format("%s.putSize(%s, %s)", bundleName, "\"" + name + "\"",
                        getStatement(isKotlinField, instance, varName));
                break;
            case "android.util.SizeF":
                statement = String.format("%s.putSizeF(%s, %s)", bundleName, "\"" + name + "\"",
                        getStatement(isKotlinField, instance, varName));
                break;
            case "android.os.Parcelable[]":
                statement =
                        String.format("%s.putParcelableArray(%s, %s)", bundleName, "\"" + name + "\"",
                                getStatement(isKotlinField, instance, varName));
                break;
            case "byte[]":
                statement = String.format("%s.putByteArray(%s, %s)", bundleName, "\"" + name + "\"",
                        getStatement(isKotlinField, instance, varName));
                break;
            case "short[]":
                statement = String.format("%s.putShortArray(%s, %s)", bundleName, "\"" + name + "\"",
                        getStatement(isKotlinField, instance, varName));
                break;
            case "char[]":
                statement = String.format("%s.putCharArray(%s, %s)", bundleName, "\"" + name + "\"",
                        getStatement(isKotlinField, instance, varName));
                break;
            case "float[]":
                statement = String.format("%s.putFloatArray(%s, %s)", bundleName, "\"" + name + "\"",
                        getStatement(isKotlinField, instance, varName));
                break;
            case "java.lang.CharSequence[]":
                statement =
                        String.format("%s.putCharSequenceArray(%s, %s)", bundleName, "\"" + name + "\"",
                                getStatement(isKotlinField, instance, varName));
                break;
        }

        if (statement == null) {

            Types typeUtil = processingEnv.getTypeUtils();
            Elements elementUtil = processingEnv.getElementUtils();

            if (typeUtil.isSubtype(element.asType(),
                    elementUtil.getTypeElement("android.os.Parcelable").asType())) {
                statement = String.format("%s.putParcelable(%s, %s)", bundleName, "\"" + name + "\"",
                        getStatement(isKotlinField, instance, varName));
            } else if (typeUtil.isSubtype(element.asType(),
                    elementUtil.getTypeElement("java.io.Serializable").asType())) {
                statement = String.format("%s.putSerializable(%s, %s)", bundleName, "\"" + name + "\"",
                        getStatement(isKotlinField, instance, varName));
            }
        }

        if (statement != null) {
            methodBuilder.addStatement(statement);
        }
    }

    static void statementGetValueFromBundle(ProcessingEnvironment processingEnv,
                                            boolean isKotlinClass, MethodSpec.Builder methodBuilder, Element element,
                                            String instance, String bundleName) {
        String statement = null;
        String varName = element.getSimpleName().toString();
        String name = element.getAnnotation(AutoField.class).value();
        if (name.isEmpty()) name = varName;
        //
        boolean isKotlinField = isKotlinClass && isKotlinField(element);
        //It's a strong conversion.
        /*intent.putIntegerArrayListExtra()
         intent.putCharSequenceArrayListExtra()
         intent.putParcelableArrayListExtra()
         intent.putStringArrayListExtra()*/
        //
        switch (element.asType().toString()) {
            case "int":
                statement = assignStatement(isKotlinField, instance, varName,
                        String.format("%s.getInt(%s)", bundleName, "\"" + name + "\""));
                break;
            case "long":
                statement = assignStatement(isKotlinField, instance, varName,
                        String.format("%s.getLong(%s)", bundleName, "\"" + name + "\""));
                break;
            case "char":
                statement = assignStatement(isKotlinField, instance, varName,
                        String.format("%s.getChar(%s)", bundleName, "\"" + name + "\""));
                break;
            case "short":
                statement = assignStatement(isKotlinField, instance, varName,
                        String.format("%s.getShort(%s)", bundleName, "\"" + name + "\""));
                break;
            case "byte":
                statement = assignStatement(isKotlinField, instance, varName,
                        String.format("%s.getByte(%s)", bundleName, "\"" + name + "\""));
                break;
            case "float":
                statement = assignStatement(isKotlinField, instance, varName,
                        String.format("%s.getFloat(%s)", bundleName, "\"" + name + "\""));
                break;
            case "double":
                statement = assignStatement(isKotlinField, instance, varName,
                        String.format("%s.getDouble(%s)", bundleName, "\"" + name + "\""));
                break;
            case "boolean":
                statement = assignStatement(isKotlinField, instance, varName,
                        String.format("%s.getBoolean(%s)", bundleName, "\"" + name + "\""));
                break;
            case "java.lang.Integer":
            case "java.lang.Long":
            case "java.lang.Character":
            case "java.lang.Short":
            case "java.lang.Byte":
            case "java.lang.Float":
            case "java.lang.Double":
            case "java.lang.Boolean":
                statement = assignStatement(isKotlinField, instance, varName,
                        String.format("(%s)%s.getSerializable(%s)", element.asType().toString(), bundleName,
                                "\"" + name + "\""));
                break;
            case "java.lang.String":
                statement = assignStatement(isKotlinField, instance, varName,
                        String.format("%s.getString(%s)", bundleName, "\"" + name + "\""));
                break;
            case "java.io.Serializable":
                statement = assignStatement(isKotlinField, instance, varName,
                        String.format("%s.getSerializable(%s)", bundleName, "\"" + name + "\""));
                break;
            case "android.os.IBinder":
                statement = assignStatement(isKotlinField, instance, varName,
                        String.format("%s.getBinder(%s)", bundleName, "\"" + name + "\""));
                break;
            case "android.os.Bundle":
                statement = assignStatement(isKotlinField, instance, varName,
                        String.format("%s.getBundle(%s)", bundleName, "\"" + name + "\""));
                break;
            case "java.lang.CharSequence":
                statement = assignStatement(isKotlinField, instance, varName,
                        String.format("%s.getCharSequence(%s)", bundleName, "\"" + name + "\""));
                break;
            case "android.os.Parcelable":
                statement = assignStatement(isKotlinField, instance, varName,
                        String.format("%s.getParcelable(%s)", bundleName, "\"" + name + "\""));
                break;
            case "android.util.Size":
                statement = assignStatement(isKotlinField, instance, varName,
                        String.format("%s.getSize(%s)", bundleName, "\"" + name + "\""));
                break;
            case "android.util.SizeF":
                statement = assignStatement(isKotlinField, instance, varName,
                        String.format("%s.getSizeF(%s)", bundleName, "\"" + name + "\""));
                break;
            case "android.os.Parcelable[]":
                statement = assignStatement(isKotlinField, instance, varName,
                        String.format("%s.getParcelableArray(%s)", bundleName, "\"" + name + "\""));
                break;
            case "byte[]":
                statement = assignStatement(isKotlinField, instance, varName,
                        String.format("%s.getByteArray(%s)", bundleName, "\"" + name + "\""));
                break;
            case "short[]":
                statement = assignStatement(isKotlinField, instance, varName,
                        String.format("%s.getShortArray(%s)", bundleName, "\"" + name + "\""));
                break;
            case "char[]":
                statement = assignStatement(isKotlinField, instance, varName,
                        String.format("%s.getCharArray(%s)", bundleName, "\"" + name + "\""));
                break;
            case "float[]":
                statement = assignStatement(isKotlinField, instance, varName,
                        String.format("%s.getFloatArray(%s)", bundleName, "\"" + name + "\""));
                break;
            case "java.lang.CharSequence[]":
                statement = assignStatement(isKotlinField, instance, varName,
                        String.format("%s.getCharSequenceArray(%s)", bundleName, "\"" + name + "\""));
                break;
        }

        if (statement == null) {
            Types typeUtil = processingEnv.getTypeUtils();
            Elements elementUtil = processingEnv.getElementUtils();

            if (typeUtil.isSubtype(element.asType(),
                    elementUtil.getTypeElement("android.os.Parcelable").asType())) {
                statement = assignStatement(isKotlinField, instance, varName,
                        String.format("(%s)%s.getParcelable(%s)", element.asType().toString(), bundleName,
                                "\"" + name + "\""));

            } else if (typeUtil.isSubtype(element.asType(),
                    elementUtil.getTypeElement("java.io.Serializable").asType())) {
                statement = assignStatement(isKotlinField, instance, varName,
                        String.format("(%s)%s.getSerializable(%s)", element.asType().toString(), bundleName,
                                "\"" + name + "\""));
            }
        }
        if (statement != null) {
            methodBuilder.addStatement(statement);
        }
    }

    private static boolean isKotlinField(Element element) {
        List<? extends Element> testGetterAndSetters =
                element.getEnclosingElement().getEnclosedElements();
        for (int i = 0; testGetterAndSetters != null && i < testGetterAndSetters.size(); i++) {
            String getter = kotlinGetterForVar(element.getSimpleName().toString());
            String setter = kotlinSetterForVar(element.getSimpleName().toString());
            String testName = testGetterAndSetters.get(i).getSimpleName().toString();
            if (testGetterAndSetters.get(i) instanceof ExecutableElement) {
                if (getter.equals(testName) || setter.equals(testName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String kotlinSetterForVar(String var) {
        if (var.startsWith("is")) {
            return var.replaceFirst("is", "set");
        } else {
            return "set" + var.substring(0, 1).toUpperCase() + (var.length() > 1 ? var.substring(1) : "");
        }
    }

    private static String kotlinGetterForVar(String var) {
        if (var.startsWith("is")) {
            return var;
        } else {
            return "get" + var.substring(0, 1).toUpperCase() + (var.length() > 1 ? var.substring(1) : "");
        }
    }

    private static String javaAssignStatement(String instance, String field, String value) {
        return String.format("%s.%s = %s", instance, field, value);
    }

    private static String kotlinAssignStatement(String instance, String field, String value) {
        return String.format("%s.%s(%s)", instance, kotlinSetterForVar(field), value);
    }

    private static String assignStatement(boolean isKotlinField, String instance, String field,
                                          String value) {
        return isKotlinField ? kotlinAssignStatement(instance, field, value)
                : javaAssignStatement(instance, field, value);
    }

    private static String javaGetStatement(String instance, String field) {
        return String.format("%s.%s", instance, field);
    }

    private static String kotlinGetStatement(String instance, String field) {
        return String.format("%s.%s()", instance, kotlinGetterForVar(field));
    }

    private static String getStatement(boolean isKotlinField, String instance, String field) {
        return isKotlinField ? kotlinGetStatement(instance, field) : javaGetStatement(instance, field);
    }
}
