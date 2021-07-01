package com.example.asm.travel.plugin

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class TravelPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.findByType(BaseExtension::class.java)
        extension?.registerTransform(TravelTransform())
    }

}