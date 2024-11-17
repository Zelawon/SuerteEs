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

    // Resolve packaging issues with duplicate META-INF files using the new approach
    packaging {
        resources.excludes.add("META-INF/INDEX.LIST")
        resources.excludes.add("META-INF/io.netty.versions.properties")
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
    implementation("androidx.activity:activity:1.7.0")

    // RecyclerView for displaying lists and grids of items
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    // Firebase BOM to manage Firebase library versions automatically
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    // Google Play Services for GPS and location-based features
    implementation("com.google.android.gms:play-services-location:21.3.0")
    // Firebase Firestore for cloud-based database storage
    implementation("com.google.firebase:firebase-firestore:24.0.2")
    // Material Design Components for UI elements and styling
    implementation("com.google.android.material:material:1.9.0")
    // MQTT Client for real-time, lightweight communication with HiveMQ
    implementation("com.hivemq:hivemq-mqtt-client:1.3.3")

}
