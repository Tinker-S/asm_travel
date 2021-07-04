package com.example.asm.travel.plugin

import com.android.build.api.transform.*
import com.android.utils.FileUtils
import com.example.asm.travel.plugin.asm.HelloAsmCreator
import com.example.asm.travel.plugin.asm.TravelClassVisitor
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

internal object TransformHelper {
    private const val DEBUG = false

    fun transformJars(
        jarInput: JarInput,
        outputProvider: TransformOutputProvider,
        isIncremental: Boolean
    ) {
        val jarName = jarInput.name
        val status = jarInput.status
        val destFile = outputProvider.getContentLocation(
            jarName,
            jarInput.contentTypes,
            jarInput.scopes,
            Format.JAR
        )
        if (DEBUG) {
            println("[transformJars] $jarName, ${jarInput.file}, status = $status, isIncremental = $isIncremental")
        }
        if (isIncremental) {
            when (status) {
                Status.ADDED -> {
                    handleJarFile(jarInput, destFile)
                }
                Status.CHANGED -> {
                    handleJarFile(jarInput, destFile)
                }
                Status.REMOVED -> {
                    if (destFile.exists()) {
                        destFile.delete()
                    }
                }
                else -> {
                    // do nothing
                }
            }
        } else {
            handleJarFile(jarInput, destFile)
        }
    }

    fun transformDirectory(
        directoryInput: DirectoryInput,
        outputProvider: TransformOutputProvider,
        isIncremental: Boolean
    ) {
        val sourceFile = directoryInput.file
        val name = sourceFile.name
        val destDir = outputProvider.getContentLocation(
            name,
            directoryInput.contentTypes,
            directoryInput.scopes,
            Format.DIRECTORY
        )
        if (DEBUG) {
            println(
                "[transformDirectory], $name, source = ${sourceFile.absolutePath}, " +
                        "dest = ${destDir.absolutePath}, isIncremental = $isIncremental"
            )
        }
        if (isIncremental) {
            val changeFiles = directoryInput.changedFiles
            for (changeFile in changeFiles) {
                val inputFile = changeFile.key
                val status = changeFile.value
                if (DEBUG) {
                    println("[incremental] $inputFile, status = $status")
                }
                val destPath = inputFile.absolutePath.replace(
                    sourceFile.absolutePath,
                    destDir.absolutePath
                )
                val destFile = File(destPath)
                when (status) {
                    Status.REMOVED -> {
                        if (destFile.exists()) {
                            destFile.delete()
                        }
                    }
                    Status.CHANGED, Status.ADDED -> {
                        handleDirectory(inputFile, destFile)
                    }
                    else -> {
                        // do nothing
                    }
                }
            }
        } else {
            FileUtils.copyDirectory(sourceFile, destDir)
            handleDirectory(sourceFile, destDir)
        }
    }

    private fun handleJarFile(jarInput: JarInput, destFile: File) {
        if (jarInput.file == null || jarInput.file.length() == 0L) {
            println("${jarInput.file.absolutePath} is empty")
            return
        }

        val jarFile = JarFile(jarInput.file)
        val destOutputStream = JarOutputStream(FileOutputStream(destFile))
        for (jarEntry in jarFile.entries()) {
            val entryName = jarEntry.name
            val destEntry = JarEntry(entryName)
            destOutputStream.putNextEntry(destEntry)
            val inputStream = jarFile.getInputStream(jarEntry)

            if (shouldProcessClass(entryName)) {
                println("process ${jarInput.file} => $entryName")
                val destData = scanClass(inputStream)
                destData.let {
                    destOutputStream.write(it)
                }
            } else {
                IOUtils.copy(inputStream, destOutputStream)
            }

            destOutputStream.flush()
            destOutputStream.closeEntry()
            inputStream.close()
        }
        destOutputStream.close()
        jarFile.close()
    }

    private fun handleDirectory(sourceFile: File, destFile: File) {
        if (destFile.isFile) {
            if (shouldProcessClass(destFile.absolutePath)) {
                println("process $destFile")
                val inputStream = FileInputStream(destFile)
                val destData = scanClass(inputStream)
                destData.let {
                    destFile.writeBytes(it)
                }
                inputStream.close()
            }
            // generate HelloAsm class with the same directory of MainActivity.class
            if (destFile.absolutePath.endsWith("com/example/asm/travel/MainActivity.class")) {
                val destDir = File(destFile.absolutePath.replace("com/example/asm/travel/MainActivity.class", ""))
                HelloAsmCreator().genClass(destDir)
            }
        } else if (destFile.isDirectory) {
            destFile.walk().forEach {
                if (it.isFile) {
                    handleDirectory(sourceFile, it)
                }
            }
        }
    }

    private fun shouldProcessClass(name: String): Boolean {
        return name.endsWith("com/example/asm/travel/target/Caller.class")
    }

    private fun scanClass(inputStream: InputStream): ByteArray {
        val cr = ClassReader(inputStream)
        val cw = TravelClassVisitor(ClassWriter(cr, ClassWriter.COMPUTE_FRAMES))
        cr.accept(cw, ClassReader.EXPAND_FRAMES)

        return cw.toByteArray()
    }

}
