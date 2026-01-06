import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.clearday"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.clearday"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // Load API key from local.properties
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }
        val pollenApiKey = localProperties.getProperty("POLLEN_API_KEY") ?: ""
        buildConfigField("String", "POLLEN_API_KEY", "\"$pollenApiKey\"")
        
        val openWeatherApiKey = localProperties.getProperty("OPENWEATHER_API_KEY") ?: ""
        buildConfigField("String", "OPENWEATHER_API_KEY", "\"$openWeatherApiKey\"")
    }
    android.buildFeatures.buildConfig = true

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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true
    }
}

dependencies {
    // --- Jetpack Compose ---
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.activity.compose)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    // --- Firebase Auth ---
    implementation("com.google.firebase:firebase-auth:22.3.1")
    // --- Firebase Firestore ---
    implementation("com.google.firebase:firebase-firestore:25.0.0")
    // --- Kotlin Coroutines ---
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    // --- Location ---
    implementation("com.google.android.gms:play-services-location:21.3.0")
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    //okhttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}