import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}
val tmdbApiKey = localProperties.getProperty("TMDB_API_KEY") ?: ""

val versionMajor = 1
val versionMinor = 5
val versionPatch = 0
val versionBuild = 47

val appVersionCode = versionMajor * 100_000_000 + versionMinor * 100_000 + versionPatch * 1_000 + versionBuild
val appVersionName = "$versionMajor.$versionMinor.$versionPatch.$versionBuild"

android {
    namespace = "com.example.pekseries"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.pekseries"
        minSdk = 26
        //noinspection OldTargetApi
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "TMDB_API_KEY", "\"$tmdbApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
//            signingConfig = signingConfigs.getByName("debug")
            buildConfigField("String", "BASE_URL", "\"https://api.tvmaze.com\"")
        }
        debug {
            isMinifyEnabled = false
            buildConfigField("String", "BASE_URL", "\"https://api.tvmaze.com\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
        compose = true
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Core and Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Network and Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.kotlinx.coroutines.android)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)
    implementation(libs.google.firebase.analytics)
    implementation(libs.play.services.auth)

    // Media
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.androidx.media3.common)
    implementation(libs.coil.compose)

    // Debugging
    implementation(libs.timber)
    debugImplementation(libs.library)
    releaseImplementation(libs.library.no.op)

    // Hilt DI - KSP
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Hilt for Jetpack Compose and ViewModel
    implementation(libs.androidx.hilt.navigation.compose)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}