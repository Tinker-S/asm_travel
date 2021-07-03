# Asm Travel

使用`Transform`及`ASM`的一些简单实践

## 生成新类
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

## 给某个类添加新的方法
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

## 删除类的某个方法

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

## 修改类的某个方法

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


## FAQ

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

