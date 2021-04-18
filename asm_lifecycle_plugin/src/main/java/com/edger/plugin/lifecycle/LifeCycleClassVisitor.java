package com.edger.plugin.lifecycle;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author Edger Lee <edger.vip@gmail.com>
 * @date 4/18/21
 */
public class LifeCycleClassVisitor extends ClassVisitor {
    private String className;
    private String superName;

    public LifeCycleClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName,
                      String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
        this.superName = superName;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature
            , String[] exceptions) {
        System.out.println("ClassVisitor visitMethod name : " + name + ", superName : " + superName);

        MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);

        if ("androidx/appcompat/app/AppCompatActivity".equals(superName)
                || "androidx/fragment/app/Fragment".equals(superName)) {
            if (name.startsWith("onCreate") || name.startsWith("onViewCreated")) {
                return new LifeCycleMethodVisitor(mv, className, name);
            }
        }
        return mv;
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }
}
