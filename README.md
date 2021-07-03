# Asm Travel

使用`Transform`及`ASM`的一些简单实践

## 一些常用的操作

### 生成新类
```
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
```

### 给某个类添加新的方法
```
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
```

### 删除类的某个方法

visitMethod的时候判断方法名，如果是要删除的方法，返回`null`即可

```
override fun visitMethod(
    access: Int,
    name: String?,
    descriptor: String?,
    signature: String?,
    exceptions: Array<out String>?
): MethodVisitor? {
    if ("launch" == name) {
        return null
    }
    return super.visitMethod(access, name, descriptor, signature, exceptions)
}

```

### 修改类的某个方法

```
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
```

### 生成二分查找代码

```
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

```


## FAQ

### 不熟悉字节码指令怎么办

推荐一款`Android Studio`上可用的插件[ASM Bytecode Viewer Support Kotlin](https://plugins.jetbrains.com/plugin/14860-asm-bytecode-viewer-support-kotlin)，直接展示源代码对应的字节码，可用来参考。

### 添加transform之后apk无法安装，`INSTALL_FAILED_INVALID_APK`

只要添加了`transfrom`，即使没有对`class`做任何处理，也要执行一下复制操作，否则`class`不会打包进`apk`中，会出现问题

```
input.jarInputs.forEach { jarInput ->
    val dest = outputProvider?.getContentLocation(
        jarInput.name,
        jarInput.contentTypes,
        jarInput.scopes,
        Format.JAR
    )
    // 必不可少
    FileUtils.copyFile(jarInput.file, dest)
}

```

### 生成代码的指令报错，不太清楚如何排查怎么办

可以使用`CheckClassAdapter`这个类来包装`ClassWriter`，有一些易懂的报错信息。
其他的一些实用的工具类也可以研究一下，例如`AnalyzerAdapter`, `LocalVariablesSorter`, `AdviceAdapter`, `TraceClassVisitor`等。
