plugins {
    alias(libs.plugins.pekseries.android.feature)
    alias(libs.plugins.pekseries.android.firebase)
}

android {
    namespace = "az.pekstudios.pekseries.feature.home"
}

dependencies {
    implementation(projects.core.network)

    implementation(libs.coil.compose)

    // TODO(P1): HomeScreen reads FirebaseAuth straight from the Composable.
    //  That belongs behind a repository so the screen can be previewed and tested.
    implementation(libs.firebase.auth)
}
