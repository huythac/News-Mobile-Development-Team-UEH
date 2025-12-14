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
    defaultConfig { vectorDrawables.useSupportLibrary = true }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // ===== FIREBASE (DÙNG BOM) =====
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))

    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-analytics")

    // ===== GOOGLE SIGN-IN (MỚI) =====
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // IMAGE
    implementation("com.squareup.picasso:picasso:2.8")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

