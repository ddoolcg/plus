package com.lcg.processor;

import com.squareup.javapoet.ClassName;

public class Constant {
    static final String ACTIVITY_AUTO_FIELD_EXTRAS = "AutoFieldExtras";
    static final String ACTIVITY_INTENT = "IntentBuilder";
    static final String FRAGMENT_BUNDLE = "BundleBuilder";

    static final ClassName BUNDLE_CLASS = ClassName.get("android.os", "Bundle");
    static final ClassName NONNULL_CLASS = ClassName.get("android.support.annotation", "NonNull");
    static final ClassName INTENT_CLASS = ClassName.get("android.content", "Intent");
    static final ClassName CONTEXT_CLASS = ClassName.get("android.content", "Context");

    static final String CLASS_ACTIVITY = "android.app.Activity";
    static final String CLASS_FRAGMENT_ACTIVITY = "android.support.v4.app.FragmentActivity";
    static final String CLASS_V4_FRAGMENT = "android.support.v4.app.Fragment";
    static final String CLASS_FRAGMENT = "android.app.Fragment";
}
