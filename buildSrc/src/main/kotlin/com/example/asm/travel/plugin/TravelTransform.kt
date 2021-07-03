package com.example.asm.travel.plugin

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.io.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import java.io.File
import java.io.FileInputStream
import java.util.jar.JarFile

class TravelTransform : Transform() {

    override fun transform(transformInvocation: TransformInvocation?) {
        super.transform(transformInvocation)
        println("transform")
        val outputProvider = transformInvocation?.outputProvider
        transformInvocation?.inputs?.forEach { input ->
            input.jarInputs.forEach { jarInput ->
                val dest = outputProvider?.getContentLocation(
                    jarInput.name,
                    jarInput.contentTypes,
                    jarInput.scopes,
                    Format.JAR
                )

                val jarFile = JarFile(jarInput.file)
                val enumeration = jarFile.entries()
                while (enumeration.hasMoreElements()) {
                    val jarEntry = enumeration.nextElement()
                    println("${jarInput.name} ${jarEntry.name} ${jarEntry.realName}")
                }
                jarFile.close()

                FileUtils.copyFile(jarInput.file, dest)
            }
            input.directoryInputs.forEach { directoryInput ->
                val dest = outputProvider?.getContentLocation(
                    directoryInput.name,
                    directoryInput.contentTypes,
                    directoryInput.scopes,
                    Format.DIRECTORY
                )

                directoryInput.file.walk().forEach {
                    println("directoryInput $it")
                    addMethodForCall(it)
                }

                FileUtils.copyDirectory(directoryInput.file, dest)

                if (directoryInput.file.absolutePath.contains("kotlin-classes")) {
                    dest?.let {
                        genNewClass(dest)
                    }
                }
            }
        }
    }

    private fun addMethodForCall(file: File) {
        if (file.absolutePath.endsWith("com/example/asm/travel/target/Caller.class")) {
            val classReader = ClassReader(FileInputStream(file))
            val classWriter = CallerClassWriter(ClassWriter(ClassWriter.COMPUTE_MAXS))
            classReader.accept(classWriter, 0)

            // 添加一个方法 log
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

            file.writeBytes(classWriter.toByteArray())
        }
    }

    private fun genNewClass(dest: File) {
        val classFullName = "com/example/asm/travel/autogen/HelloAsm"
        val helloAsmFile = File("${dest}/${classFullName}.class")
        if (helloAsmFile.exists()) {
            helloAsmFile.delete()
        }
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

    override fun getName(): String {
        return "AsmTravel"
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean {
        return false
    }

    internal class CallerClassWriter(private val writer: ClassWriter) : ClassVisitor(ASM7, writer) {

        override fun visitMethod(
            access: Int,
            name: String?,
            descriptor: String?,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor {
            val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
            if ("launch" == name) {
                return CallerMethodVisitor(mv)
            }
            return mv
        }

        fun toByteArray(): ByteArray {
            return writer.toByteArray()
        }
    }

    internal class CallerMethodVisitor(mv: MethodVisitor) : MethodVisitor(ASM7, mv) {

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
}