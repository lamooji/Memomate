plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    id("com.google.devtools.ksp")
    id("androidx.navigation.safeargs.kotlin")
    id("io.github.takahirom.roborazzi") version "1.8.0-alpha-5"
    id ("kotlin-parcelize")
}

android {
    namespace = "com.cs407.memoMate"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.cs407.memoMate"
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

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

//dependencies {
//
//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.appcompat)
//    implementation(libs.material)
//    implementation(libs.androidx.activity)
//    implementation(libs.androidx.constraintlayout)
//    implementation(libs.androidx.navigation.fragment.ktx)
//    implementation(libs.androidx.navigation.ui.ktx)
//
//    //calendar
//    implementation(libs.calendar.compose)
//    implementation(libs.calendar.view)
//
//    implementation(libs.androidx.paging.common.android)
//    implementation(libs.room.ktx)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
//    implementation("androidx.cardview:cardview:1.0.0")
//
//
//    implementation(libs.androidx.room.common)
//    implementation(libs.androidx.room.compiler)
//    implementation(libs.androidx.room.runtime)
//    implementation(libs.androidx.room.ktx)
//}

dependencies {

    // Core libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation("androidx.cardview:cardview:1.0.0")

    // Calendar libraries
    implementation(libs.calendar.compose)
    implementation(libs.calendar.view)

    // Paging
    implementation(libs.androidx.paging.common.android)

    // Room (keep only the necessary dependencies)
    implementation(libs.androidx.room.runtime) // Core Room library
    implementation(libs.androidx.room.paging)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    implementation(libs.androidx.ui.android)
    implementation(libs.play.services.gcm)

    annotationProcessor(libs.androidx.room.compiler)
    ksp(libs.androidx.room.compiler)       // Room annotation processor (used with kapt)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.android.volley:volley:1.2.0")

    // Optional: JSON parsing library (for JSONObject)
    implementation("org.json:json:20230227")
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains:annotations:23.0.0")
    }
}