package az.pekstudios.pekseries.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * Compile settings shared by every Android module.
 *
 * Note that AGP 9 compiles Kotlin itself (built-in Kotlin support), so no module
 * applies org.jetbrains.kotlin.android. The `kotlin` extension seen here is
 * registered by AGP's KotlinBaseApiPlugin.
 *
 * AGP 9's CommonExtension exposes plain getters rather than the Action-taking
 * `defaultConfig { }` overloads of AGP 8, hence the property/apply style below.
 */
internal fun Project.configureKotlinAndroid(extension: CommonExtension) {
    extension.compileSdk = PekBuildConfig.COMPILE_SDK

    extension.defaultConfig.minSdk = PekBuildConfig.MIN_SDK

    extension.compileOptions.apply {
        sourceCompatibility = PekBuildConfig.JAVA_VERSION
        targetCompatibility = PekBuildConfig.JAVA_VERSION
    }

    extension.lint.apply {
        // Lint errors should stop CI; warnings stay advisory so a dependency
        // bump that adds a new warning does not block a release.
        abortOnError = true
        warningsAsErrors = false
        xmlReport = true
        htmlReport = true
    }

    tasks.withType(Test::class.java).configureEach {
        // Gradle 9 fails a Test task that discovers nothing. In a multi-module
        // build that is normal: :core:network and :core:ui carry no unit tests,
        // and an aggregate `./gradlew test` should not fail because of it.
        failOnNoDiscoveredTests.set(false)
    }

    extensions.configure<KotlinAndroidProjectExtension> {
        jvmToolchain(PekBuildConfig.JVM_TOOLCHAIN)

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}
