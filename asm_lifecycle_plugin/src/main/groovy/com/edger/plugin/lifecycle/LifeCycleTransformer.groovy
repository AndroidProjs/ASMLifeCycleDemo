package com.edger.plugin.lifecycle

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import groovy.io.FileType
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

class LifeCycleTransformer extends Transform {

    @Override
    String getName() {
        return "LifeCycleTransformer"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.PROJECT_ONLY
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation)
            throws TransformException, InterruptedException, IOException {
        // 拿到所有的class文件
        Collection<TransformInput> transformInputs = transformInvocation.inputs
        TransformOutputProvider outputProvider = transformInvocation.outputProvider
        if (outputProvider != null) {
            outputProvider.deleteAll()
        }

        transformInputs.each { transformInput ->
            // 遍历 directoryInputs(文件夹中的 class 文件)
            // directoryInputs 代表着以源码方式参与项目编译的所有目录结构及其目录下的源码文件
            // 比如我们手写的类以及 R.class、BuildConfig.class 以及 MainActivity.class 等
            transformInput.directoryInputs.each { directoryInput ->
                File dir = directoryInput.file
                if (dir) {
                    dir.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) { file ->
                        System.out.println("find class: " + file.name)
                        // 对 class 文件进行读取与解析
                        ClassReader classReader = new ClassReader(file.bytes)
                        // 对 class 文件的写入
                        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                        // 访问 class 文件对应的内容，解析到某一个结构就会通知到 ClassVisitor 的相应方法
                        ClassVisitor classVisitor = new LifeCycleClassVisitor(classWriter)
                        // 依次调用 ClassVisitor 接口的各个方法
                        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
                        // toByteArray 方法会将最终修改的字节码以 byte 数组形式返回
                        byte[] bytes = classWriter.toByteArray()

                        // 通过文件流写入方式覆盖掉原先的内容，实现class文件的改写。
                        // FileOutputStream outputStream = new FileOutputStream( file.parentFile.absolutePath + File.separator + fileName)
                        FileOutputStream outputStream = new FileOutputStream(file.path)
                        outputStream.write(bytes)
                        outputStream.close()
                    }
                }

                // 处理完输入文件后把输出传给下一个文件
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                FileUtils.copyDirectory(directoryInput.file, dest)
            }

            // Gradle 3.6.0 以上 R 类不会转为 .class 文件而会转成 jar，因此在 Transform 实现中需要单独拷贝
            transformInput.jarInputs.each { JarInput jarInput ->
                File file = jarInput.file
                System.out.println("find jar : " + file.name)
                def dest = outputProvider.getContentLocation(jarInput.name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                FileUtils.copyFile(file, dest)
            }
        }
    }
}