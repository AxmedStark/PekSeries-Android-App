package az.pekstudios.pekseries.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Project

const val FLAVOR_DIMENSION = "environment"

enum class PekFlavor(
    val flavorName: String,
    val applicationIdSuffix: String?,
    val versionNameSuffix: String?,
) {
    /** Local and internal-distribution builds. Verbose logging, Chucker on. */
    Dev(flavorName = "dev", applicationIdSuffix = ".debug", versionNameSuffix = "-DEV"),

    /** What ships to Google Play. */
    Prod(flavorName = "prod", applicationIdSuffix = null, versionNameSuffix = null),
}

/**
 * Only the application module declares flavors. Library modules deliberately
 * stay flavourless: AGP can serve a flavourless library to a flavoured consumer,
 * and giving every module flavors would force a missingDimensionStrategy on all
 * of them for no benefit.
 */
internal fun Project.configureFlavors(extension: ApplicationExtension) {
    // Side-by-side installs need a second Android app registered in the Firebase
    // console for the suffixed id, otherwise google-services fails the build.
    val useSeparateAppId = providers
        .gradleProperty(PekBuildConfig.DEV_SEPARATE_APP_ID_PROPERTY)
        .orNull
        ?.toBoolean() == true

    extension.apply {
        flavorDimensions += FLAVOR_DIMENSION

        productFlavors {
            PekFlavor.entries.forEach { flavor ->
                create(flavor.flavorName) {
                    dimension = FLAVOR_DIMENSION

                    if (flavor == PekFlavor.Dev) {
                        versionNameSuffix = flavor.versionNameSuffix
                        if (useSeparateAppId) {
                            applicationIdSuffix = flavor.applicationIdSuffix
                        }
                    }

                    buildConfigField(
                        "String",
                        "ENVIRONMENT",
                        "\"${flavor.flavorName.uppercase()}\"",
                    )
                }
            }
        }
    }

    if (!useSeparateAppId) {
        logger.info(
            "PekSeries: dev flavor shares the applicationId with prod. " +
                "Register '${PekBuildConfig.APPLICATION_ID}.debug' in the Firebase console, " +
                "then set ${PekBuildConfig.DEV_SEPARATE_APP_ID_PROPERTY}=true to install both builds at once.",
        )
    }
}
