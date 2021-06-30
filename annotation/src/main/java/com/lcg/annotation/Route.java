package com.lcg.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 路由
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface Route {
    /**
     * path
     */
    String value();
}