package com.example.asm.travel.plugin.asm

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*

class TravelClassVisitor(private val cw: ClassWriter) : ClassVisitor(ASM7, cw) {

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        if ("com/example/asm/travel/target/Caller" == name) {
            addLogMethodForCall(cw)
        }
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
        if ("launch" == name) {
            return CallerLaunchMethodVisitor(mv)
        }
        return mv
    }

    fun toByteArray(): ByteArray {
        return cw.toByteArray()
    }

    /**
     * 给 Caller 类添加一个方法 log(tag: String, message: String)
     */
    private fun addLogMethodForCall(classWriter: ClassWriter) {
        val methodVisitor = classWriter.visitMethod(
            ACC_PUBLIC + ACC_FINAL,
            "log",
            "(Ljava/lang/String;Ljava/lang/String;)V",
            null,
            null
        )
        methodVisitor.visitVarInsn(ALOAD, 1)
        methodVisitor.visitVarInsn(ALOAD, 2)
        methodVisitor.visitMethodInsn(
            INVOKESTATIC,
            "android/util/Log",
            "d",
            "(Ljava/lang/String;Ljava/lang/String;)I",
            false
        )
        methodVisitor.visitInsn(RETURN)
        methodVisitor.visitMaxs(1, 1)
        methodVisitor.visitEnd()
        classWriter.visitEnd()
    }
}