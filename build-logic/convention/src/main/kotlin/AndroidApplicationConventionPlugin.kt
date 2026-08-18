import az.pekstudios.pekseries.buildlogic.PekBuildConfig
import az.pekstudios.pekseries.buildlogic.RELEASE_SIGNING_CONFIG
import az.pekstudios.pekseries.buildlogic.configureFlavors
import az.pekstudios.pekseries.buildlogic.configureKotlinAndroid
import az.pekstudios.pekseries.buildlogic.configureReleaseSigning
import az.pekstudios.pekseries.buildlogic.readAppVersion
import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")

        val appVersion = readAppVersion()

        extensions.configure<ApplicationExtension> {
            configureKotlinAndroid(this)

            val signed = configureReleaseSigning(this)

            defaultConfig {
                applicationId = PekBuildConfig.APPLICATION_ID
                targetSdk = PekBuildConfig.TARGET_SDK
                versionCode = appVersion.versionCode
                versionName = appVersion.versionName

                testInstrumentationRunner = PekBuildConfig.TEST_RUNNER
            }

            configureFlavors(this)

            buildTypes {
                debug {
                    isMinifyEnabled = false
                    isShrinkResources = false
                }

                release {
                    isMinifyEnabled = true
                    isShrinkResources = true
                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"),
                        "proguard-rules.pro",
                    )
                    if (signed) {
                        signingConfig = signingConfigs.getByName(RELEASE_SIGNING_CONFIG)
                    }
                }
            }

            buildFeatures {
                buildConfig = true
            }

            packaging {
                resources {
                    excludes += setOf(
                        "/META-INF/{AL2.0,LGPL2.1}",
                        "/META-INF/LICENSE*",
                        "/META-INF/DEPENDENCIES",
                        "META-INF/*.version",
                    )
                }
            }

            // Play serves per-device splits, so keep the bundle splitting on.
            bundle {
                language { enableSplit = true }
                density { enableSplit = true }
                abi { enableSplit = true }
            }
        }
    }
}
