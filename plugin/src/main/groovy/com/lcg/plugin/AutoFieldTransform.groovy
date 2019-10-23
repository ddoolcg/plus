package com.lcg.plugin

import com.android.build.api.transform.*
import com.google.common.collect.Sets
import com.lcg.plugin.zip.Compressor
import com.lcg.plugin.zip.Decompression
import groovy.io.FileType
import javassist.ClassPath
import javassist.ClassPool
import javassist.CtClass
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

import java.lang.reflect.Constructor

class AutoFieldTransform extends Transform {
    Project mProject

    AutoFieldTransform(Project project) {
        mProject = project
    }

    @Override
    String getName() {
        return "auto-field"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return Collections.singleton(QualifiedContent.DefaultContentType.CLASSES)
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        if (mProject.plugins.hasPlugin("com.android.application")) {
            return Sets.immutableEnumSet(
                    QualifiedContent.Scope.PROJECT,
                    QualifiedContent.Scope.SUB_PROJECTS,
                    QualifiedContent.Scope.EXTERNAL_LIBRARIES)
        } else if (mProject.plugins.hasPlugin("com.android.library")) {
            return Sets.immutableEnumSet(
                    QualifiedContent.Scope.PROJECT)
        } else {
            return Collections.emptySet()
        }
    }

    @Override
    boolean isIncremental() {
        return true
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        ClassPool classPool = ClassPool.getDefault()
        def classPath = []
        classPool.appendClassPath(mProject.android.bootClasspath[0].toString())
        try {
            Class jarClassPathClazz = Class.forName("javassist.JarClassPath")
            Constructor constructor = jarClassPathClazz.getDeclaredConstructor(String.class)
            constructor.setAccessible(true)

            transformInvocation.inputs.each { input ->
                def subProjectInputs = []
                input.jarInputs.each { jarInput ->
                    ClassPath clazzPath = (ClassPath) constructor.newInstance(jarInput.file.absolutePath)
                    classPath.add(clazzPath)
                    classPool.appendClassPath(clazzPath)

                    def jarName = jarInput.name
                    if (jarName.endsWith(".jar")) {
                        jarName = jarName.substring(0, jarName.length() - 4)
                    }
                    if (jarName.startsWith(":")) {
                        subProjectInputs.add(jarInput)
                    } else {
                        def dest = transformInvocation.outputProvider.getContentLocation(jarName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                        FileUtils.copyFile(jarInput.file, dest)
                    }
                }

                // Handle library project jar here
                subProjectInputs.each { jarInput ->
                    def jarName = jarInput.name
                    if (jarName.endsWith(".jar")) {
                        jarName = jarName.substring(0, jarName.length() - 4)
                    }

                    if (jarName.startsWith(":")) {
                        // sub project
                        File unzipDir = new File(jarInput.file.getParent(), jarName.replace(":", "") + "_unzip")
                        if (unzipDir.exists()) {
                            unzipDir.delete()
                        }
                        unzipDir.mkdirs()
                        Decompression.uncompress(jarInput.file, unzipDir)

                        File repackageFolder = new File(
                                jarInput.file.getParent(),
                                jarName.replace(":", "") + "_repackage"
                        )

                        FileUtils.copyDirectory(unzipDir, repackageFolder)

                        unzipDir.eachFileRecurse(FileType.FILES) { File it ->
                            checkAndTransformClass(classPool, it, repackageFolder)
                        }

                        // re-package the folder to jar
                        def dest = transformInvocation.outputProvider.getContentLocation(
                                jarName, jarInput.contentTypes, jarInput.scopes, Format.JAR)

                        Compressor zc = new Compressor(dest.getAbsolutePath())
                        zc.compress(repackageFolder.getAbsolutePath())
                    }
                }

                input.directoryInputs.each { dirInput ->
                    def outDir = transformInvocation.outputProvider.getContentLocation(dirInput.name, dirInput.contentTypes, dirInput.scopes, Format.DIRECTORY)
                    classPool.appendClassPath(dirInput.file.absolutePath)
                    // dirInput.file is like "build/intermediates/classes/debug"
                    int pathBitLen = dirInput.file.toString().length()

                    def callback = { File it ->
                        if (it.exists()) {
                            def path = "${it.toString().substring(pathBitLen)}"
                            if (it.isDirectory()) {
                                new File(outDir, path).mkdirs()
                            } else {
                                boolean handled = checkAndTransformClass(classPool, it, outDir)
                                if (!handled) {
                                    // copy the file to output location
                                    new File(outDir, path).bytes = it.bytes
                                }
                            }
                        }
                    }

                    if (dirInput.changedFiles != null && dirInput.changedFiles.isEmpty()) {
                        dirInput.changedFiles.keySet().each(callback)
                    }
                    if (dirInput.file != null && dirInput.file.exists() && dirInput.file.isDirectory()) {
                        dirInput.file.traverse(callback)
                    }
                }
            }
        } finally {
            // release File Handlers in ClassPool
            classPath.each { it ->
                classPool.removeClassPath(it)
            }
        }

    }


    boolean checkAndTransformClass(ClassPool classPool, File file, File dest) {
        classPool.importPackage("android.os.Bundle")
        classPool.importPackage("android.view.LayoutInflater")
        classPool.importPackage("android.view.ViewGroup")

        CtClass fragmentActivityCtClass
        CtClass v4FragmentCtClass
        try {
            fragmentActivityCtClass = classPool.get("android.support.v4.app.FragmentActivity")
            v4FragmentCtClass = classPool.get("android.support.v4.app.Fragment")
        } catch (Throwable t) {
            //v4
        }
        CtClass activityCtClass = classPool.get("android.app.Activity")
        CtClass fragmentCtClass = classPool.get("android.app.Fragment")

        if (!file.name.endsWith("class")) {
            return false
        }

        CtClass ctClass
        try {
            ctClass = classPool.makeClass(new FileInputStream(file))
        } catch (Throwable throwable) {
            mProject.logger.error("Parsing class file ${file.getAbsolutePath()} fail.", throwable)
            return false
        }
        // Support Activity and AppCompatActivity
        boolean handled
        if (ctClass.subclassOf(activityCtClass) || (fragmentActivityCtClass != null && ctClass.subclassOf(fragmentActivityCtClass))) {
            ActivityAutoFieldTransform transform = new ActivityAutoFieldTransform(mProject, ctClass, classPool)
            transform.handleActivitySaveState()
            handled = true
        } else if (ctClass.subclassOf(fragmentCtClass) || (v4FragmentCtClass != null && ctClass.subclassOf(v4FragmentCtClass))) {
            FragmentAutoFieldTransform transform = new FragmentAutoFieldTransform(ctClass, classPool, mProject)
            transform.handleFragmentSaveState()
            handled = true
        }
        if (handled) {
            ctClass.writeFile(dest.absolutePath)
            ctClass.detach()
        }
        return handled
    }
}