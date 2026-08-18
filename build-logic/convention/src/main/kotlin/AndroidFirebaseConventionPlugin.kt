import az.pekstudios.pekseries.buildlogic.library
import az.pekstudios.pekseries.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Pins Firebase to the BOM in every consuming module. Without this each module
 * would be free to resolve a different Firebase version, which surfaces as
 * confusing NoSuchMethodErrors at runtime rather than as a build failure.
 */
class AndroidFirebaseConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        dependencies {
            add("implementation", platform(libs.library("firebase-bom")))
        }
    }
}
