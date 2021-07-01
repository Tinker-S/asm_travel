package com.example.asm.travel.classloader

class AsmClassLoader : ClassLoader() {

    fun defineClass(name: String?, data: ByteArray): Class<*> {
        return defineClass(name, data, 0, data.size)
    }

}