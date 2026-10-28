plugins {
    id("com.android.application")
}

android {
    namespace = "tv.guard.switcher"
    compileSdk = 34
    defaultConfig {
        applicationId = "tv.guard.switcher"
        minSdk = 24
        targetSdk = 34
        versionCode = 6
        versionName = "1.5"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}
