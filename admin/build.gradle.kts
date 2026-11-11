import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy
import java.util.Base64

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.admin"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.aistudio.jummoadmin.panel"
        minSdk = 24
        targetSdk = 36
        versionCode = 240
        versionName = "2.4.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        if (!file("google-services.json").exists()) {
            resValue("string", "default_web_client_id", "")
        }
    }

    val keystoreFile = file("../debug.keystore")
    val keystoreBase64File = file("../debug.keystore.base64")

    if (!keystoreFile.exists() && keystoreBase64File.exists()) {
        val base64Content = keystoreBase64File.readText().replace("\\s".toRegex(), "")
        keystoreFile.writeBytes(Base64.getDecoder().decode(base64Content))
    }

    signingConfigs {
        create("release") {
            storeFile = keystoreFile
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        getByName("debug") {
            storeFile = keystoreFile
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.systemProperty(
                    "robolectric.dependency.repo.url",
                    "https://maven-central.storage-download.googleapis.com/maven2/"
                )
            }
        }
    }
}

googleServices { missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN }

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.kotlinx.coroutines.android)

    // Firebase Auth + Firestore
    implementation(platform(libs.firebase.bom))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation("androidx.test:core:1.6.1")
    testImplementation(libs.androidx.junit)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
