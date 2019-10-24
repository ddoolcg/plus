package com.lcg.processor;

import com.squareup.javapoet.ClassName;

public class Constant {
    public static final String ACTIVITY_AUTO_FIELD_EXTRAS = "AutoFieldExtras";
    public static final String ACTIVITY_INTENT = "Intent%sBuilder";
    public static final String FRAGMENT_BUNDLE = "Bundle%sBuilder";

    public static final ClassName BUNDLE_CLASS = ClassName.get("android.os", "Bundle");
    public static final ClassName NONNULL_CLASS = ClassName.get("android.support.annotation", "NonNull");
    public static final ClassName INTENT_CLASS = ClassName.get("android.content", "Intent");
    public static final ClassName CONTEXT_CLASS = ClassName.get("android.content", "Context");
    public static final ClassName ACTIVITY_CLASS = ClassName.get("android.app", "Activity");

    public static final String CLASS_ACTIVITY = "android.app.Activity";
    public static final String CLASS_FRAGMENT_ACTIVITY = "android.support.v4.app.FragmentActivity";
    public static final String CLASS_V4_FRAGMENT = "android.support.v4.app.Fragment";
    public static final String CLASS_FRAGMENT = "android.app.Fragment";
    public static final String CLASS_BASE_OBSERVABLE_ME = "com.lcg.mylibrary.BaseObservableMe";
}
