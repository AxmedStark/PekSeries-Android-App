plugins {
    alias(libs.plugins.pekseries.android.library)
    alias(libs.plugins.pekseries.android.hilt)
}

android {
    namespace = "az.pekstudios.pekseries.core.datastore"
}

dependencies {
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.timber)

    testImplementation(projects.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
}
