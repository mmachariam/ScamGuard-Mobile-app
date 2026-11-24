plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.scamguard"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.scamguard"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    // Saves the first-launch preference in app storage.
    implementation(libs.androidx.datastore.preferences)
    // Connects ViewModels and lifecycle-aware state collection to Compose.
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    // Manages screen navigation and the back stack.
    implementation(libs.androidx.navigation.compose)
    // Runtime database APIs.
    implementation(libs.androidx.room.runtime)
    // Generates the database and DAO implementations during compilation.
    ksp(libs.androidx.room.compiler)
    // Sends analysis requests to the Python API.
    implementation(libs.okhttp)
}

// Export database schemas so future migrations can preserve existing history.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}