plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "dte.masteriot.mdp.suertees"
    compileSdk = 34

    defaultConfig {
        applicationId = "dte.masteriot.mdp.suertees"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("androidx.recyclerview:recyclerview:1.2.1") //recycler view
    implementation(platform("com.google.firebase:firebase-bom:33.4.0")) // Latest BOM version
    implementation("com.google.android.gms:play-services-location:21.3.0") //For GPS
    implementation("com.google.firebase:firebase-firestore:24.0.2") // Cloud Firestore
    implementation("com.google.android.material:material:1.9.0") // Material Design Dependency
}
