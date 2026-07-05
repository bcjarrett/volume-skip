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
        val appVersion = System.getenv("VERSION_NAME") ?: "0.0.0"
        val vp = appVersion.split(".").map { it.toIntOrNull() ?: 0 }
        versionName = appVersion
        versionCode = maxOf(1, vp.getOrElse(0) { 0 } * 10000 + vp.getOrElse(1) { 0 } * 100 + vp.getOrElse(2) { 0 })
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
