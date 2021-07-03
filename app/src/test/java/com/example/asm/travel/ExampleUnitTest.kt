package com.example.asm.travel

import com.example.asm.travel.utils.ReflectUtil
import org.junit.Test

import org.junit.Assert.*
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.util.CheckClassAdapter

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

        ReflectUtil.callStaticMethod(
            ReflectUtil.findMethod(
                ReflectUtil.loadClass("Hello", cw.toByteArray()),
                "main",
                Array<String>::class.java
            ),
            arrayOf<String>()
        )
    }

    @Test
    fun genBinarySearchByAsm() {
        val writer = ClassWriter(ClassWriter.COMPUTE_FRAMES)
        val cw = CheckClassAdapter(writer)

        cw.visit(V1_8, ACC_PUBLIC, "Hello", null, "java/lang/Object", null)

        var mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null)
        mv.visitCode()
        mv.visitInsn(ICONST_5)
        mv.visitIntInsn(NEWARRAY, T_INT)
        mv.visitInsn(DUP)
        mv.visitInsn(ICONST_0)
        mv.visitInsn(ICONST_1)
        mv.visitInsn(IASTORE)
        mv.visitInsn(DUP)
        mv.visitInsn(ICONST_1)
        mv.visitInsn(ICONST_2)
        mv.visitInsn(IASTORE)
        mv.visitInsn(DUP)
        mv.visitInsn(ICONST_2)
        mv.visitInsn(ICONST_3)
        mv.visitInsn(IASTORE)
        mv.visitInsn(DUP)
        mv.visitInsn(ICONST_3)
        mv.visitInsn(ICONST_5)
        mv.visitInsn(IASTORE)
        mv.visitInsn(DUP)
        mv.visitInsn(ICONST_4)
        mv.visitIntInsn(BIPUSH, 6)
        mv.visitInsn(IASTORE)
        mv.visitVarInsn(ASTORE, 1)
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        mv.visitVarInsn(ALOAD, 1)
        mv.visitInsn(ICONST_2)
        mv.visitMethodInsn(INVOKESTATIC, "com/example/asm/travel/target/Test", "binarySearch", "([II)I", false)
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false)
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        mv.visitVarInsn(ALOAD, 1)
        mv.visitInsn(ICONST_4)
        mv.visitMethodInsn(INVOKESTATIC, "com/example/asm/travel/target/Test", "binarySearch", "([II)I", false)
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false)
        mv.visitInsn(RETURN)
        mv.visitMaxs(4, 2)
        mv.visitEnd()

        mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "binarySearch", "([II)I", null, null)
        mv.visitCode()
        mv.visitVarInsn(ALOAD, 0)
        val l0 = Label()
        mv.visitJumpInsn(IFNULL, l0)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitInsn(ARRAYLENGTH)
        val l1 = Label()
        mv.visitJumpInsn(IFNE, l1)
        mv.visitLabel(l0)
        mv.visitInsn(ICONST_M1)
        mv.visitInsn(IRETURN)
        mv.visitLabel(l1)
        mv.visitInsn(ICONST_0)
        mv.visitVarInsn(ISTORE, 2)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitInsn(ARRAYLENGTH)
        mv.visitInsn(ICONST_1)
        mv.visitInsn(ISUB)
        mv.visitVarInsn(ISTORE, 3)
        val l2 = Label()
        mv.visitLabel(l2)
        mv.visitVarInsn(ILOAD, 2)
        mv.visitVarInsn(ILOAD, 3)
        val l3 = Label()
        mv.visitJumpInsn(IF_ICMPGT, l3)
        mv.visitVarInsn(ILOAD, 2)
        mv.visitVarInsn(ILOAD, 3)
        mv.visitVarInsn(ILOAD, 2)
        mv.visitInsn(ISUB)
        mv.visitInsn(ICONST_2)
        mv.visitInsn(IDIV)
        mv.visitInsn(IADD)
        mv.visitVarInsn(ISTORE, 4)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitVarInsn(ILOAD, 4)
        mv.visitInsn(IALOAD)
        mv.visitVarInsn(ILOAD, 1)
        val l4 = Label()
        mv.visitJumpInsn(IF_ICMPNE, l4)
        mv.visitVarInsn(ILOAD, 4)
        mv.visitInsn(IRETURN)
        mv.visitLabel(l4)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitVarInsn(ILOAD, 4)
        mv.visitInsn(IALOAD)
        mv.visitVarInsn(ILOAD, 1)
        val l5 = Label()
        mv.visitJumpInsn(IF_ICMPGE, l5)
        mv.visitVarInsn(ILOAD, 4)
        mv.visitInsn(ICONST_1)
        mv.visitInsn(IADD)
        mv.visitVarInsn(ISTORE, 2)
        val l6 = Label()
        mv.visitJumpInsn(GOTO, l6)
        mv.visitLabel(l5)
        mv.visitVarInsn(ILOAD, 4)
        mv.visitInsn(ICONST_1)
        mv.visitInsn(ISUB)
        mv.visitVarInsn(ISTORE, 3)
        mv.visitLabel(l6)
        mv.visitJumpInsn(GOTO, l2)
        mv.visitLabel(l3)
        mv.visitInsn(ICONST_M1)
        mv.visitInsn(IRETURN)
        mv.visitMaxs(3, 5)
        mv.visitEnd()

        cw.visitEnd()

        ReflectUtil.callStaticMethod(
            ReflectUtil.findMethod(
                ReflectUtil.loadClass("Hello", writer.toByteArray()),
                "main",
                Array<String>::class.java
            ),
            arrayOf<String>()
        )
    }
}