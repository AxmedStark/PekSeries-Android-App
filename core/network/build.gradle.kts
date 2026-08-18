import java.util.Properties

plugins {
    alias(libs.plugins.pekseries.android.library)
    alias(libs.plugins.pekseries.android.hilt)
}

// Environment first so CI never has to write a file to disk; secrets.properties
// is the local-development fallback and is gitignored.
val secrets = Properties().apply {
    rootProject.file("secrets.properties").takeIf { it.exists() }?.inputStream()?.use(::load)
}
val tmdbApiKey: String = System.getenv("TMDB_API_KEY") ?: secrets.getProperty("TMDB_API_KEY") ?: ""

android {
    namespace = "az.pekstudios.pekseries.core.network"

    defaultConfig {
        buildConfigField("String", "TMDB_API_KEY", "\"$tmdbApiKey\"")
        buildConfigField("String", "TMDB_BASE_URL", "\"https://api.themoviedb.org/3/\"")
        buildConfigField("String", "TVMAZE_BASE_URL", "\"https://api.tvmaze.com/\"")
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    api(projects.core.model)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.timber)

    // Chucker is referenced unconditionally from NetworkModule, so the release
    // variant needs the no-op artifact that keeps the same API surface.
    debugImplementation(libs.chucker.debug)
    releaseImplementation(libs.chucker.release)
}
