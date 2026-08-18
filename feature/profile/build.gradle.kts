plugins {
    alias(libs.plugins.pekseries.android.feature)
    alias(libs.plugins.pekseries.android.firebase)
}

android {
    namespace = "az.pekstudios.pekseries.feature.profile"
}

dependencies {
    implementation(projects.core.domain)

    implementation(libs.androidx.activity.compose)
    implementation(libs.coil.compose)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.messaging)
}
