plugins {
    alias(libs.plugins.pekseries.android.library)
    alias(libs.plugins.pekseries.android.library.compose)
}

android {
    namespace = "az.pekstudios.pekseries.core.ui"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.model)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.material.icons.extended)
}
