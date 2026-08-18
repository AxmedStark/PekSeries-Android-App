plugins {
    alias(libs.plugins.pekseries.android.feature)
    alias(libs.plugins.pekseries.android.room)
}

android {
    namespace = "az.pekstudios.pekseries.feature.notifications"
}

dependencies {
    implementation(projects.core.network)
}
