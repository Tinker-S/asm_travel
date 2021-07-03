package com.example.asm.travel.utils

import com.example.asm.travel.classloader.AsmClassLoader
import java.lang.reflect.Method

object ReflectUtil {

    fun loadClass(fullName: String, data: ByteArray): Class<*> {
        val classLoader = AsmClassLoader()
        return classLoader.defineClass(fullName, data)
    }

    fun findMethod(
        clazz: Class<*>,
        name: String,
        vararg parameterTypes: Class<*>
    ): Method {
        return clazz.getMethod(name, *parameterTypes)
    }

    fun callMethod(
        target: Any?,
        method: Method,
        vararg args: Any
    ) {
        try {
            method.invoke(target, *args)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun callStaticMethod(
        method: Method,
        vararg args: Any
    ) {
        try {
            method.invoke(null, *args)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}