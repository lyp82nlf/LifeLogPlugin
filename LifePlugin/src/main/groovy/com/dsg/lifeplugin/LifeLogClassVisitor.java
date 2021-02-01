package com.dsg.lifeplugin;

import com.dsg.lifeplugin.utils.LogUtils;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASM4;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.RETURN;

/**
 * @author DSG
 * @Project LifeCyclePlugin
 * @date 2021/1/29
 * @describe
 */
public class LifeLogClassVisitor extends ClassVisitor {
    public boolean changed = false;
    private boolean isActivity = false;
    private boolean isFragment = false;
    private boolean hasOnCreate = false;
    private String superName;

    public LifeLogClassVisitor(ClassVisitor classVisitor) {
        super(ASM4, classVisitor);
    }

    public LifeLogClassVisitor(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        LogUtils.log("current class:" + name + "  super name:" + superName);
        isActivity = superName.toLowerCase().contains("activity");
        isFragment = superName.toLowerCase().contains("fragment");
        this.superName = superName;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if ((isActivity || isFragment) && name.equals("onCreate")) {
            hasOnCreate = true;
            changed = true;
            LogUtils.log("insert method ++++++++++++");
            mv = new LifeLogMethodVisitor(ASM4, mv, isActivity);
        }
        return mv;
    }


    @Override
    public void visitEnd() {
        super.visitEnd();
        if (isActivity || isFragment) {
            if (!hasOnCreate) {
                changed = true;
                LogUtils.log("need onCreate");
                MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "onCreate", "(Landroid/os/Bundle;)V", null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitMethodInsn(INVOKESPECIAL, superName, "onCreate", "(Landroid/os/Bundle;)V", false);
                //打印log
                mv.visitLdcInsn("DSG");
                mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
                mv.visitLdcInsn(String.format("current %s: ", isActivity ? "activity" : "fragment"));
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getSimpleName", "()Ljava/lang/String;", false);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
                mv.visitMethodInsn(INVOKESTATIC, "android/util/Log", "d", "(Ljava/lang/String;Ljava/lang/String;)I", false);
                mv.visitInsn(POP);
                mv.visitInsn(RETURN);
                mv.visitMaxs(3, 2);
                mv.visitEnd();
            }
        }
    }
}