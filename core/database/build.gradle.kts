plugins {
    alias(libs.plugins.pekseries.android.library)
    alias(libs.plugins.pekseries.android.hilt)
    alias(libs.plugins.pekseries.android.room)
}

android {
    namespace = "az.pekstudios.pekseries.core.database"
}

dependencies {
    implementation(projects.core.model)
}
