package com.edger.plugin.lifecycle

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

public class LifeCyclePlugin implements Plugin<Project> {
    void apply(Project project) {
        System.out.println("> LifeCyclePlugin gradle plugin")

        def android = project.extensions.getByType(AppExtension)
        println('> registering AutoTrackTransform')
        LifeCycleTransformer transformer = new LifeCycleTransformer()
        android.registerTransform(transformer)
    }
}