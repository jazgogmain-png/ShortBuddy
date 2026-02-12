plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // 1. ADD THE KSP PLUGIN HERE
    id("com.google.devtools.ksp") version "1.9.22-1.0.17"
}

android {
    namespace = "com.shor.tbuddy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.shor.tbuddy"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0-Ninja"

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

    buildFeatures {
        viewBinding = true
        dataBinding = false
        compose = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE*"
            excludes += "META-INF/NOTICE*"
            excludes += "META-INF/INDEX.LIST"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Coroutines & Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // 2. ADD ROOM DEPENDENCIES HERE
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // YouTube API
    implementation("com.google.apis:google-api-services-youtube:v3-rev20251217-2.0.0") {
        exclude(group = "org.apache.httpcomponents")
        exclude(group = "com.google.guava", module = "listenablefuture")
    }
    implementation("com.google.api-client:google-api-client-android:2.2.0")
    implementation("com.google.http-client:google-http-client-gson:1.43.3")

    // THE FULL MEDIA3 STACK
    val media3Version = "1.2.1"
    implementation("androidx.media3:media3-transformer:$media3Version")
    implementation("androidx.media3:media3-exoplayer:$media3Version")
    implementation("androidx.media3:media3-ui:$media3Version")
    implementation("androidx.media3:media3-common:$media3Version")

    // Gemini 3 Flash Preview (SDK 0.9.0)
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // Google Auth & Play Services
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.android.gms:play-services-base:18.2.0")

    testImplementation("junit:junit:4.13.2")
}