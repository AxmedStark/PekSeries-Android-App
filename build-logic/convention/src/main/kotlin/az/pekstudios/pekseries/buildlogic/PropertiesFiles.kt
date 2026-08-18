package az.pekstudios.pekseries.buildlogic

import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.File
import java.util.Properties

private fun File.loadProperties(): Properties = Properties().also { props ->
    if (exists()) inputStream().use(props::load)
}

/**
 * App version, read from version.properties at the repository root.
 *
 * Keeping the numbers in a plain properties file (rather than inline in
 * app/build.gradle.kts) means CI can bump a single line with sed and does not
 * have to rewrite Kotlin source to cut a release.
 */
data class AppVersion(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val build: Int,
) {
    /**
     * Monotonic and human-decodable: 1.8.3.58 becomes 100_808_058.
     * Google Play requires versionCode to increase on every upload and caps it
     * at 2_100_000_000, which this scheme stays well under.
     */
    val versionCode: Int = major * 100_000_000 + minor * 100_000 + patch * 1_000 + build

    /** Full four-part name, used for internal/dev builds. */
    val versionName: String = "$major.$minor.$patch.$build"

    /** Three-part semver, what users see on a store build. */
    val marketingVersionName: String = "$major.$minor.$patch"
}

internal fun Project.readAppVersion(): AppVersion {
    val file = rootProject.layout.projectDirectory.file("version.properties").asFile
    if (!file.exists()) {
        throw GradleException(
            "version.properties not found at ${file.absolutePath}. " +
                "It is a committed file; restore it from git.",
        )
    }
    val props = file.loadProperties()

    fun read(key: String): Int = props.getProperty(key)?.trim()?.toIntOrNull()
        ?: throw GradleException("version.properties is missing a numeric '$key' entry")

    return AppVersion(
        major = read("VERSION_MAJOR"),
        minor = read("VERSION_MINOR"),
        patch = read("VERSION_PATCH"),
        build = read("VERSION_BUILD"),
    )
}

/**
 * Secrets, resolved from the environment first and secrets.properties second.
 *
 * CI sets real environment variables and never writes the file; developers keep
 * a gitignored secrets.properties locally. Neither path puts a credential into
 * version control, which is the point of moving off local.properties.
 */
class Secrets(private val props: Properties) {
    operator fun get(key: String): String = System.getenv(key) ?: props.getProperty(key) ?: ""

    fun require(key: String): String = get(key).ifEmpty {
        throw GradleException(
            "Secret '$key' is not set. Export it as an environment variable or add it to secrets.properties.",
        )
    }

    fun has(key: String): Boolean = get(key).isNotEmpty()
}

internal fun Project.readSecrets(): Secrets = Secrets(
    rootProject.layout.projectDirectory.file("secrets.properties").asFile.loadProperties(),
)
