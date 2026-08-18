pluginManagement {
    includeBuild("build-logic")

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

// Lets modules be referenced as projects.core.model instead of project(":core:model"),
// which is typo-proof and navigable from the IDE.
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "PekSeries"

include(":app")

// Core
include(":core:data")
include(":core:database")
include(":core:datastore")
include(":core:domain")
include(":core:model")
include(":core:network")
include(":core:ui")
include(":core:work")

// Features
include(":feature:auth")
include(":feature:detail")
include(":feature:home")
include(":feature:notifications")
include(":feature:profile")
include(":feature:search")
include(":feature:watchlist")
