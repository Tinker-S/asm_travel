package com.example.asm.travel

import com.example.asm.travel.classloader.AsmClassLoader
import org.junit.Test

import org.junit.Assert.*
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.*
import java.lang.Exception

class ExampleUnitTest {

    @Test
    fun helloWorldByAsm() {
        val cw = ClassWriter(0)
        cw.visit(V1_8, ACC_PUBLIC, "Hello", null, "java/lang/Object", null)
        val mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null)
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        mv.visitLdcInsn("Hello, World")
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false)
        mv.visitInsn(RETURN)
        mv.visitMaxs(2, 1)
        mv.visitEnd()
        cw.visitEnd()

        val classLoader = AsmClassLoader()
        val clazz = classLoader.defineClass("Hello", cw.toByteArray())
        try {
            val main = clazz.getMethod("main", Array<String>::class.java)
            main.invoke(null, arrayOf<String>())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}