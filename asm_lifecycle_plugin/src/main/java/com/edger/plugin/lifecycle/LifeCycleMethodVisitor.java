package com.edger.plugin.lifecycle;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author Edger Lee <edger.vip@gmail.com>
 * @date 4/18/21
 */
public class LifeCycleMethodVisitor extends MethodVisitor {
    private final String className;
    private final String methodName;

    public LifeCycleMethodVisitor(MethodVisitor methodVisitor, String className, String methodName) {
        super(Opcodes.ASM5, methodVisitor);
        this.className = className;
        this.methodName = methodName;
    }

    @Override
    public void visitCode() {
        super.visitCode();
        System.out.println("    MethodVisitor visitCode -> ");

        mv.visitLdcInsn("TAG");
        mv.visitLdcInsn(className + " -> " + methodName);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                "android/util/Log",
                "i",
                "(Ljava/lang/String;Ljava/lang/String;)I",
                false);
        mv.visitLdcInsn(Opcodes.POP);
    }
}
