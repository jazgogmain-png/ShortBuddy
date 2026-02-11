plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
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

    // YouTube API
    implementation("com.google.apis:google-api-services-youtube:v3-rev20251217-2.0.0") {
        exclude(group = "org.apache.httpcomponents")
        exclude(group = "com.google.guava", module = "listenablefuture")
    }
    implementation("com.google.api-client:google-api-client-android:2.2.0")
    implementation("com.google.http-client:google-http-client-gson:1.43.3")

    // Media3 Transformer
    implementation("androidx.media3:media3-transformer:1.2.1")
    implementation("androidx.media3:media3-common:1.2.1")

    // Gemini 3 Flash Preview (SDK 0.1.2)
    implementation("com.google.ai.client.generativeai:generativeai:0.1.2")

    testImplementation("junit:junit:4.13.2")

    // Google Auth & Play Services (The 'Pro' Handshake)
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.android.gms:play-services-base:18.2.0")
}