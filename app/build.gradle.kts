plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.liferlighdow.vnavi"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.liferlighdow.vnavi"
        minSdk = 21
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Zero dependencies for extreme lightweight performance.
}
