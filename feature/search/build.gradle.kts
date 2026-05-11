plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "az.pekstudios.pekseries.feature.search"
    compileSdk = 36

    defaultConfig { minSdk = 26 }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
}

kotlin { jvmToolchain(17) }

dependencies {
    // 1. Подключаем НАШИ модули-кирпичи
    implementation(project(":core:network"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))

    // 2. Базовый Compose (чтобы верстать экран)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // 3. Hilt (для внедрения ViewModel)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // 4. Корутины и Coil (для картинок постеров)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.coil.compose)
}