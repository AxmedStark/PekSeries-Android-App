package az.pekstudios.pekseries.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.dependencies

/**
 * Enables Compose and pins every module to the same Compose BOM, so a single
 * catalog bump moves all modules together and versions cannot skew.
 */
internal fun Project.configureAndroidCompose(extension: CommonExtension) {
    pluginManager.apply(libs.findPlugin("kotlin-compose").get().get().pluginId)

    extension.buildFeatures.compose = true

    dependencies {
        val bom = platform(libs.library("androidx-compose-bom"))
        add("implementation", bom)
        add("androidTestImplementation", bom)

        add("implementation", libs.library("androidx-compose-ui"))
        add("implementation", libs.library("androidx-compose-ui-graphics"))
        add("implementation", libs.library("androidx-compose-ui-tooling-preview"))
        add("implementation", libs.library("androidx-compose-material3"))

        add("debugImplementation", libs.library("androidx-compose-ui-tooling"))
        add("debugImplementation", libs.library("androidx-compose-ui-test-manifest"))
    }
}

internal fun DependencyHandler.implementation(dependency: Any) {
    add("implementation", dependency)
}
