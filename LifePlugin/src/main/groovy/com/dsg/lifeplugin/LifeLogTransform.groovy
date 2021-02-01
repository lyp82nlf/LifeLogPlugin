package com.dsg.lifeplugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.dsg.lifeplugin.utils.EnvUtils
import com.dsg.lifeplugin.utils.LogUtils
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

class LifeLogTransform extends Transform {

    @Override
    String getName() {
        return "LifeLog"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        def startTime = System.currentTimeMillis()

        for (TransformInput input in inputs) {
            for (DirectoryInput dirInput in input.directoryInputs) {
                if (EnvUtils.getInstance().isDebug()) {
                    modifyClassWithPath(dirInput.file)
                }
                File dest = outputProvider.getContentLocation(dirInput.name, dirInput.contentTypes,
                        dirInput.scopes, Format.DIRECTORY)
                FileUtils.copyDirectory(dirInput.file, dest)
            }
            for (JarInput jarInput in input.jarInputs) {
                copyFile(jarInput, outputProvider)
            }
        }
        def cost = (System.currentTimeMillis() - startTime) / 1000
        LifeLogPlugin.logger.quiet "LifeLogPlugin cost $cost second"
        LifeLogPlugin.logger.quiet '================LifeLogPlugin end================'
    }


    void modifyClassWithPath(File dir) {
        def root = dir.absolutePath
        dir.eachFileRecurse { File file ->
            def filePath = file.absolutePath
            //过滤非class文件
            if (!filePath.endsWith(".class")) return
            def className = getClassName(root, filePath)
            //过滤系统文件
            if (isSystemClass(className)) return
            //hook关键代码
            hookClass(filePath, className)
        }
    }

    void hookClass(String filePath, String className) {
        //1.声明ClassReader
        ClassReader reader = new ClassReader(new FileInputStream(new File(filePath)))
        //2声明 ClassWriter
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS)
        //3声明ClassVisitor
        LifeLogClassVisitor adapter = new LifeLogClassVisitor(writer)
        //4调用accept方法 传入classVisitor
        reader.accept(adapter, ClassReader.EXPAND_FRAMES)
        if (adapter.changed) {
            LogUtils.log(className + "is changed:" + adapter.changed)
            byte[] bytes = writer.toByteArray()
            FileOutputStream fos = new FileOutputStream(new File(filePath))
            fos.write(bytes)
        }
    }

    //默认排除
    static final DEFAULT_EXCLUDE = [
            '^android\\..*',
            '^androidx\\..*',
            '.*\\.R$',
            '.*\\.R\\$.*$',
            '.*\\.BuildConfig$',
    ]

    //获取类名
    String getClassName(String root, String classPath) {
        return classPath.substring(root.length() + 1, classPath.length() - 6)
                .replaceAll("/", ".")       // unix/linux
                .replaceAll("\\\\", ".")    //windows
    }

    boolean isSystemClass(String fileName) {
        for (def exclude : DEFAULT_EXCLUDE) {
            if (fileName.matches(exclude)) return true
        }
        return false
    }

    void copyFile(JarInput jarInput, TransformOutputProvider outputProvider) {
        def dest = getDestFile(jarInput, outputProvider)
        FileUtils.copyFile(jarInput.file, dest)
    }

    static File getDestFile(JarInput jarInput, TransformOutputProvider outputProvider) {
        def destName = jarInput.name
        // 重名名输出文件,因为可能同名,会覆盖
        def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath)
        if (destName.endsWith(".jar")) {
            destName = destName.substring(0, destName.length() - 4)
        }
        // 获得输出文件
        File dest = outputProvider.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
        return dest
    }

}