package az.pekstudios.pekseries.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

/**
 * Gives convention plugins access to the same `libs` version catalog that the
 * regular build scripts use. Gradle does not generate typed accessors for
 * precompiled script plugins, so the catalog has to be looked up by name.
 */
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.version(alias: String): String =
    findVersion(alias).orElseThrow {
        IllegalStateException("Version '$alias' is missing from gradle/libs.versions.toml")
    }.requiredVersion

internal fun VersionCatalog.library(alias: String) =
    findLibrary(alias).orElseThrow {
        IllegalStateException("Library '$alias' is missing from gradle/libs.versions.toml")
    }

internal fun VersionCatalog.bundle(alias: String) =
    findBundle(alias).orElseThrow {
        IllegalStateException("Bundle '$alias' is missing from gradle/libs.versions.toml")
    }
