plugins {
    alias(libs.plugins.jetbrains.kotlin.android)
    id ("com.android.library")
    id ("kotlin-android")
    id ("kotlin-parcelize")
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.youtube.domain"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        targetSdk = 34

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
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation (libs.androidx.hilt.navigation.compose)
    ksp (libs.androidx.hilt.compiler)
    implementation(libs.hilt.android)
    implementation (libs.androidx.work.runtime.ktx)
    implementation (libs.androidx.hilt.work)
    implementation(libs.retrofit)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.converter.gson)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}