plugins {
    alias(libs.plugins.pekseries.android.library)
    alias(libs.plugins.pekseries.android.hilt)
    alias(libs.plugins.pekseries.android.firebase)
}

android {
    namespace = "az.pekstudios.pekseries.core.data"
}

dependencies {
    api(projects.core.domain)
    implementation(projects.core.model)
    implementation(projects.core.datastore)
    implementation(projects.core.network)

    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.retrofit)
    implementation(libs.timber)

    testImplementation(projects.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
}
