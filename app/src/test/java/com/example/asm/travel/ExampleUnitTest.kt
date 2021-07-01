package com.example.asm.travel

import com.example.asm.travel.utils.ReflectUtil
import org.junit.Test

import org.junit.Assert.*
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.*

class ExampleUnitTest {

    @Test
    fun genHelloWorldByAsm() {
        val cw = ClassWriter(ClassWriter.COMPUTE_MAXS)
        cw.visit(V1_8, ACC_PUBLIC, "Hello", null, "java/lang/Object", null)
        val mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null)
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        mv.visitLdcInsn("Hello, World")
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false)
        mv.visitInsn(RETURN)
        mv.visitMaxs(1, 1)
        mv.visitEnd()
        cw.visitEnd()

        ReflectUtil.callMethod(
            ReflectUtil.findMethod(
                ReflectUtil.loadClass("Hello", cw.toByteArray()),
                "main",
                Array<String>::class.java
            ),
            arrayOf<String>()
        )
    }

    @Test
    fun genSubclassByAsm() {
        val cw = ClassWriter(0)
        cw.visit(V1_8, ACC_PUBLIC, "Hello", null, "java/lang/Object", null)
    }
}