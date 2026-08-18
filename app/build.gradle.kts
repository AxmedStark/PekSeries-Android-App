plugins {
    alias(libs.plugins.pekseries.android.application)
    alias(libs.plugins.pekseries.android.application.compose)
    alias(libs.plugins.pekseries.android.hilt)
    alias(libs.plugins.pekseries.android.firebase)
    alias(libs.plugins.google.services)
}

android {
    namespace = "az.pekstudios.pekseries"
}

dependencies {
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.model)
    implementation(projects.core.network)
    implementation(projects.core.ui)
    implementation(projects.core.work)

    implementation(projects.feature.auth)
    implementation(projects.feature.detail)
    implementation(projects.feature.home)
    implementation(projects.feature.notifications)
    implementation(projects.feature.profile)
    implementation(projects.feature.search)
    implementation(projects.feature.watchlist)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.messaging)

    implementation(libs.timber)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
