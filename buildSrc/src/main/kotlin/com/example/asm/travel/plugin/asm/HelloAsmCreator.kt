package com.example.asm.travel.plugin.asm

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.*
import java.io.File

class HelloAsmCreator {

    fun genClass(dest: File) {
        val classFullName = "com/example/asm/travel/autogen/HelloAsm"
        val helloAsmFile = File("${dest}/${classFullName}.class")

        val cw = ClassWriter(ClassWriter.COMPUTE_MAXS)
        cw.visit(V1_8, ACC_PUBLIC, classFullName, null, "java/lang/Object", null)
        val mv = cw.visitMethod(
            ACC_PUBLIC + ACC_STATIC,
            "showToast",
            "(Landroid/content/Context;Ljava/lang/String;)V",
            null,
            null
        )
        mv.visitVarInsn(ALOAD, 0)
        mv.visitLdcInsn("context")
        mv.visitMethodInsn(
            INVOKESTATIC,
            "kotlin/jvm/internal/Intrinsics",
            "checkNotNullParameter",
            "(Ljava/lang/Object;Ljava/lang/String;)V",
            false
        )
        mv.visitVarInsn(ALOAD, 1)
        mv.visitLdcInsn("message")
        mv.visitMethodInsn(
            INVOKESTATIC,
            "kotlin/jvm/internal/Intrinsics",
            "checkNotNullParameter",
            "(Ljava/lang/Object;Ljava/lang/String;)V",
            false
        )
        mv.visitVarInsn(ALOAD, 0)
        mv.visitVarInsn(ALOAD, 1)
        mv.visitTypeInsn(CHECKCAST, "java/lang/CharSequence")
        mv.visitInsn(ICONST_0)
        mv.visitMethodInsn(
            INVOKESTATIC,
            "android/widget/Toast",
            "makeText",
            "(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;",
            false
        )
        mv.visitMethodInsn(
            INVOKEVIRTUAL,
            "android/widget/Toast",
            "show",
            "()V",
            false
        )
        mv.visitInsn(RETURN)
        mv.visitMaxs(1, 1)
        mv.visitEnd()
        cw.visitEnd()

        if (!helloAsmFile.parentFile.exists()) {
            helloAsmFile.parentFile.mkdirs()
        }
        helloAsmFile.writeBytes(cw.toByteArray())
    }
}