plugins {
    alias(libs.plugins.pekseries.android.feature)
    alias(libs.plugins.pekseries.android.firebase)
}

android {
    namespace = "az.pekstudios.pekseries.feature.auth"
}

dependencies {
    implementation(projects.core.network)

    implementation(libs.firebase.auth)
    implementation(libs.play.services.auth)
    implementation(libs.kotlinx.coroutines.play.services)
}
