import az.pekstudios.pekseries.buildlogic.PekBuildConfig
import az.pekstudios.pekseries.buildlogic.configureKotlinAndroid
import az.pekstudios.pekseries.buildlogic.library
import az.pekstudios.pekseries.buildlogic.libs
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")

        extensions.configure<LibraryExtension> {
            configureKotlinAndroid(this)

            defaultConfig {
                testInstrumentationRunner = PekBuildConfig.TEST_RUNNER

                // Opt-in: a module only declares consumer rules if it actually
                // ships something R8 must not touch (reflected DTOs, entities).
                val consumerRules = file(PekBuildConfig.CONSUMER_PROGUARD_FILE)
                if (consumerRules.exists()) {
                    consumerProguardFiles(consumerRules)
                }
            }
        }

        dependencies {
            add("testImplementation", libs.library("junit"))
            add("androidTestImplementation", libs.library("androidx-junit"))
        }
    }
}
