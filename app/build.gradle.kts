plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "phivu.ueh.edu.vn.news_app"
    compileSdk = 36

    defaultConfig {
        applicationId = "phivu.ueh.edu.vn.news_app"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase Realtime Database
    implementation("com.google.firebase:firebase-database:20.3.0")

    // Firebase Auth (nếu bạn dùng login)
    implementation("com.google.firebase:firebase-auth:22.1.0")

    // Firebase Analytics (khuyên dùng)
    implementation("com.google.firebase:firebase-analytics:21.5.1")

    // Google sign in (nếu cần)
    implementation("com.google.android.gms:play-services-auth:20.5.0")

    // Image loader
    implementation("com.squareup.picasso:picasso:2.8")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
