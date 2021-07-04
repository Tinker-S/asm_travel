package com.example.asm.travel.plugin.asm

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*

class CallerLaunchMethodVisitor(mv: MethodVisitor) : MethodVisitor(ASM7, mv) {

    override fun visitCode() {
        super.visitCode()
        mv.visitFieldInsn(
            GETSTATIC,
            "com/example/asm/travel/target/Caller",
            "TAG",
            "Ljava/lang/String;",
        )
        mv.visitLdcInsn("start")
        mv.visitMethodInsn(
            INVOKESTATIC,
            "android/util/Log",
            "d",
            "(Ljava/lang/String;Ljava/lang/String;)I",
            false
        )
    }

    override fun visitInsn(opcode: Int) {
        if (opcode == RETURN) {
            mv.visitFieldInsn(
                GETSTATIC,
                "com/example/asm/travel/target/Caller",
                "TAG",
                "Ljava/lang/String;",
            )
            mv.visitLdcInsn("end")
            mv.visitMethodInsn(
                INVOKESTATIC,
                "android/util/Log",
                "d",
                "(Ljava/lang/String;Ljava/lang/String;)I",
                false
            )
        }
        super.visitInsn(opcode)
    }

}