pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PekSeries"
include(":app")
include(":core:network")
include(":core:model")
include(":core:ui")
include(":feature:search")
include(":feature:watchlist")
include(":feature:profile")
include(":feature:auth")
include(":feature:home")
include(":feature:notifications")
include(":feature:detail")
include(":core:work")
