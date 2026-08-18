plugins {
    alias(libs.plugins.pekseries.android.feature)
}

android {
    namespace = "az.pekstudios.pekseries.feature.watchlist"
}

dependencies {
    implementation(projects.core.domain)

    implementation(libs.coil.compose)
}
