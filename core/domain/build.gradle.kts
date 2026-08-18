plugins {
    alias(libs.plugins.pekseries.android.library)
}

android {
    namespace = "az.pekstudios.pekseries.core.domain"
}

dependencies {
    api(projects.core.model)

    implementation(libs.javax.inject)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(projects.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
}
