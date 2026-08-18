package az.pekstudios.pekseries.buildlogic

import org.gradle.api.JavaVersion

/**
 * Single source of truth for values that must be identical across every module.
 * Previously these were copy-pasted into all twelve build files, which is how
 * they drift apart.
 */
object PekBuildConfig {
    const val APPLICATION_ID = "az.pekstudios.pekseries"

    const val COMPILE_SDK = 36
    const val MIN_SDK = 26
    const val TARGET_SDK = 36

    const val JVM_TOOLCHAIN = 17
    val JAVA_VERSION = JavaVersion.VERSION_17

    const val TEST_RUNNER = "androidx.test.runner.AndroidJUnitRunner"
    const val CONSUMER_PROGUARD_FILE = "consumer-rules.pro"

    /**
     * Opt-in gradle property. When true the `dev` flavor installs as
     * `az.pekstudios.pekseries.debug`, side by side with the Play Store build.
     *
     * This requires a matching Android app registered in the Firebase console,
     * otherwise the google-services plugin fails the build with
     * "No matching client found for package name". Defaults to false so the
     * project builds against the existing single-client google-services.json.
     */
    const val DEV_SEPARATE_APP_ID_PROPERTY = "pekseries.devSeparateApplicationId"
}
