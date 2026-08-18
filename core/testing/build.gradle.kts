plugins {
    alias(libs.plugins.pekseries.android.library)
}

android {
    namespace = "az.pekstudios.pekseries.core.testing"
}

dependencies {
    // api: consumers get the domain types and the test toolkit transitively,
    // so a module under test only declares testImplementation(projects.core.testing).
    api(projects.core.domain)
    api(projects.core.model)

    api(libs.junit)
    api(libs.truth)
    api(libs.turbine)
    api(libs.mockk)
    api(libs.kotlinx.coroutines.test)
}
