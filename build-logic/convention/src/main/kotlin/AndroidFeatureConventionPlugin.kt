import az.pekstudios.pekseries.buildlogic.library
import az.pekstudios.pekseries.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

/**
 * The shape every `:feature:*` module has in common: an Android library with
 * Compose, Hilt, the core UI/model modules, and the lifecycle plumbing a screen
 * needs. Feature modules should not need to restate any of this.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("pekseries.android.library")
        pluginManager.apply("pekseries.android.library.compose")
        pluginManager.apply("pekseries.android.hilt")

        dependencies {
            add("implementation", project(":core:model"))
            add("implementation", project(":core:ui"))

            add("implementation", libs.library("androidx-core-ktx"))
            add("implementation", libs.library("androidx-lifecycle-runtime-ktx"))
            add("implementation", libs.library("androidx-lifecycle-viewmodel-compose"))
            add("implementation", libs.library("androidx-hilt-navigation-compose"))
            add("implementation", libs.library("androidx-compose-material-icons-extended"))
            add("implementation", libs.library("kotlinx-coroutines-android"))
            add("implementation", libs.library("timber"))

            // Brings junit, truth, turbine, mockk, coroutines-test and the
            // shared fakes transitively, so a feature declares nothing extra.
            add("testImplementation", project(":core:testing"))
        }
    }
}
