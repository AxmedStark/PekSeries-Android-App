package az.pekstudios.pekseries.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Project

const val RELEASE_SIGNING_CONFIG = "release"

internal object SigningKeys {
    const val STORE_FILE = "PEKSERIES_KEYSTORE_FILE"
    const val STORE_PASSWORD = "PEKSERIES_KEYSTORE_PASSWORD"
    const val KEY_ALIAS = "PEKSERIES_KEY_ALIAS"
    const val KEY_PASSWORD = "PEKSERIES_KEY_PASSWORD"
}

/**
 * Release signing, resolved from the environment or secrets.properties.
 *
 * Nothing secret is committed: CI exports the four variables and materialises the
 * keystore from an encrypted secret; locally they live in gitignored files. When
 * the credentials are absent the signing config is simply not created, so
 * `assembleProdRelease` still produces an (unsigned) artifact for verification
 * rather than failing the whole build on a developer machine.
 *
 * @return true when a usable signing config was registered.
 */
internal fun Project.configureReleaseSigning(extension: ApplicationExtension): Boolean {
    val secrets = readSecrets()

    val keystorePath = secrets[SigningKeys.STORE_FILE]
    if (keystorePath.isEmpty()) {
        logger.info("PekSeries: no release keystore configured; release builds will be unsigned.")
        return false
    }

    val keystore = file(keystorePath).takeIf { it.exists() }
        ?: rootProject.file(keystorePath).takeIf { it.exists() }

    if (keystore == null) {
        logger.warn(
            "PekSeries: ${SigningKeys.STORE_FILE} points at '$keystorePath' but no such file exists. " +
                "Release builds will be unsigned.",
        )
        return false
    }

    extension.signingConfigs.create(RELEASE_SIGNING_CONFIG) {
        storeFile = keystore
        storePassword = secrets.require(SigningKeys.STORE_PASSWORD)
        keyAlias = secrets.require(SigningKeys.KEY_ALIAS)
        keyPassword = secrets.require(SigningKeys.KEY_PASSWORD)
    }
    return true
}
