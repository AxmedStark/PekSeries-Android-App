plugins {
    alias(libs.plugins.pekseries.android.feature)
}

android {
    namespace = "az.pekstudios.pekseries.feature.detail"
}

dependencies {
    implementation(projects.core.network)

    implementation(libs.coil.compose)
}
