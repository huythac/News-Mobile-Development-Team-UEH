plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")   // 🔥 thêm dòng này
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

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    implementation(libs.firebase.firestore)
    implementation(libs.room.common.jvm)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Firebase Realtime Database
    implementation("com.google.firebase:firebase-database:20.2.1")

    // Firebase Auth
    implementation("com.google.firebase:firebase-auth:22.1.0")

    // Google sign-in
    implementation("com.google.android.gms:play-services-auth:20.5.0")

    // Optional: image loader for displaying images later
    implementation("com.squareup.picasso:picasso:2.8")
}
