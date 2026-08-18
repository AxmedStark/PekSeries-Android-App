plugins {
    alias(libs.plugins.pekseries.android.library)
    alias(libs.plugins.pekseries.android.hilt)
    alias(libs.plugins.pekseries.android.firebase)
}

android {
    namespace = "az.pekstudios.pekseries.core.work"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.network)
    implementation(projects.core.ui)

    // TODO(P1): :core:work depends on :feature:notifications only to reach
    //  NotificationDao. That is a core -> feature inversion; it goes away once
    //  Room moves into :core:database.
    implementation(projects.feature.notifications)

    implementation(libs.firebase.messaging)

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.timber)
}
