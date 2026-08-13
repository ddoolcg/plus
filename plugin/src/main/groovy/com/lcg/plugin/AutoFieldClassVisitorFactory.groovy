package com.lcg.plugin

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import org.objectweb.asm.ClassVisitor

/**
 * AGP instrumentation entry point for auto-field bytecode changes.
 *
 * @author Lei Chuguang
 * @date 2026-08-12
 */
abstract class AutoFieldClassVisitorFactory implements AsmClassVisitorFactory<InstrumentationParameters.None> {
    @Override
    ClassVisitor createClassVisitor(ClassContext classContext, ClassVisitor nextClassVisitor) {
        return new AutoFieldClassVisitor(nextClassVisitor, classContext.currentClassData.superClasses)
    }

    @Override
    boolean isInstrumentable(ClassData classData) {
        return classData.className != null && !classData.className.endsWith(Constant.GENERATED_FILE_SUFFIX)
    }
}
