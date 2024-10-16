plugins {
    id ("com.android.library")
    id ("kotlin-android")
    id ("kotlin-parcelize")
    id ("org.jetbrains.kotlin.android")
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.com.chaquo.python)
}

android {
    namespace = "com.youtube.data"
    compileSdk = 34

    defaultConfig {

        minSdk = 24
        targetSdk= 34

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
        }
        vectorDrawables {
            useSupportLibrary = true
        }
        javaCompileOptions.annotationProcessorOptions.arguments["dagger.hilt.disableModulesHaveInstallInCheck"] =
            "true"
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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

chaquopy {
    defaultConfig {
        version = "3.8"
        buildPython("/usr/local/bin/python3")

        pip {
            install("pytubefix")
            install("requests")
        }

        pyc {
            src = false
        }
    }

    sourceSets {
        getByName("main") {
            srcDir("src/main/python")
        }
    }
}

dependencies {
    implementation(project(":domain"))
    implementation (libs.androidx.work.runtime.ktx)
    implementation (libs.androidx.hilt.navigation.compose)
    implementation (libs.androidx.hilt.work)
    annotationProcessor(libs.androidx.hilt.compiler)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.retrofit)
    ksp (libs.androidx.room.compiler)
    implementation(libs.converter.gson)
    implementation(libs.retrofit)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}