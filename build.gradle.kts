plugins {
    // Android
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false

    // Kotlin
    alias(libs.plugins.kotlin.compose) apply false

    // Hilt / KSP
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false

    // Firebase / Google Services
    alias(libs.plugins.google.services) apply false
}
