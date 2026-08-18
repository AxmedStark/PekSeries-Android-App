plugins {
    alias(libs.plugins.pekseries.android.feature)
}

android {
    namespace = "az.pekstudios.pekseries.feature.notifications"
}

dependencies {
    implementation(projects.core.database)
}
