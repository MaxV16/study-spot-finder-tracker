plugins {
    // the android application plugin, the version comes from libs.versions.toml
    alias(libs.plugins.android.application)
}

android {
    // the base package for the app, R gets generated in here
    namespace = "org.setu.studyspotfinder"
    // which android sdk we compile against
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        // the app id shown on the phone (matches the package)
        applicationId = "org.setu.studyspotfinder"
        // oldest android version we support
        minSdk = 24
        // the version we build for
        targetSdk = 37
        // app version info
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    // use java 11 language features
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// everything the app needs to run + the testing libs
dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    // material components (toolbar, buttons, text fields, cards)
    implementation(libs.material)
    // junit for the unit tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    // the recyclerview that shows the list of spots
    implementation(libs.androidx.recyclerview)

}
