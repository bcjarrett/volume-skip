plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "dev.ben.volumeskip"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.ben.volumeskip"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0" // x-release-please-version
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}
