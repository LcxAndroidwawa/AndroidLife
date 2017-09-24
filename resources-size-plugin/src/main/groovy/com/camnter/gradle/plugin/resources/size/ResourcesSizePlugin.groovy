package com.camnter.gradle.plugin.resources.size

import com.android.build.gradle.*
import com.android.build.gradle.api.BaseVariant
import com.camnter.gradle.plugin.resources.size.utils.ImageUtils
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author CaMnter
 */

class ResourcesSizePlugin implements Plugin<Project> {

    /**
     * Apply this plugin to the given target object.
     *
     * @param target The target object
     */
    @Override
    void apply(Project target) {
        println "[ResourcesSizePlugin]"
        // variants
        target.plugins.all {
            if (it instanceof FeaturePlugin) {
                FeatureExtension featureExtension = target.extensions.getByType(
                        FeatureExtension.class)
                execute(target, featureExtension.featureVariants)
            } else if (it instanceof AppPlugin) {
                AppExtension appExtension = target.extensions.getByType(AppExtension.class)
                execute(target, appExtension.applicationVariants)
            } else if (it instanceof LibraryPlugin) {
                LibraryExtension libraryExtension = target.extensions.getByType(
                        LibraryExtension.class)
                execute(target, libraryExtension.libraryVariants)
            }
        }
    }

    private void execute(Project project, DomainObjectSet<BaseVariant> variants) {
        // debug or assemble
        def taskNames = project.gradle.startParameter.taskNames
        // TODO
        def debugTask = false
        def containAssembleTask = false

        for (def taskName : taskNames) {
            if (taskName.contains("assemble") || taskName.contains("resguard")) {
                if (taskName.toLowerCase().endsWith("debug") && taskName.toLowerCase().
                        contains("debug")) {
                    debugTask = true
                }
                containAssembleTask = true
                break
            }
        }

        // TODO assemble

        project.afterEvaluate {
            variants.all {
                def resourcesDir
                if (it.productFlavors.size() == 0) {
                    resourcesDir = 'merged'
                } else {
                    resourcesDir = "merged/${it.productFlavors[0].name}"
                }

                def processResourceTask = project.tasks.findByName(
                        "process${it.name.capitalize()}Resources")
                def taskName = "resourcesSize${it.name.capitalize()}"

                project.task(taskName) {
                    doLast {
                        def resourcesDirFile = new File(
                                "${project.projectDir}/build/intermediates/res/${resourcesDir}/")
                        def bigImagePathList = []
                        resourcesDirFile.traverse {
                            def fileName = it.name
                            if (fileName.contains('drawable') || fileName.contains('mipmap')) {
                                // TODO
                                if (ImageUtils.checkImageSize(it, 1024 * 100 /*100kb*/)) {
                                    bigImagePathList << [((float) it.length() / 1024.0f).round(2) +
                                                                 ' kb', it.path]
                                }
                            }
                        }
                        if (bigImagePathList.size() > 0) {
                            println "[ResourcesSizePlugin]   [${taskName}] :"
                        }
                        bigImagePathList.each {
                            printf "%10s : %s\n", it
                        }
                    }
                }
                project.tasks.findByName(taskName).dependsOn processResourceTask.taskDependencies.
                        getDependencies(processResourceTask)
                processResourceTask.dependsOn project.tasks.findByName(taskName)
            }
        }
    }
}