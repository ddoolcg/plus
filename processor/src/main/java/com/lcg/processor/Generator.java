package com.lcg.processor;

import com.squareup.javapoet.JavaFile;

public interface Generator {
    JavaFile createSourceFile();
}
