plugins {
    alias(libs.plugins.pekseries.android.feature)
}

android {
    namespace = "az.pekstudios.pekseries.feature.search"
}

dependencies {
    implementation(projects.core.network)

    implementation(libs.coil.compose)
}
