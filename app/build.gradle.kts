plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.equilibra.app"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.equilibra.app"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // Measures how much code the unit tests cover:
            // ./gradlew createDebugUnitTestCoverageReport
            enableUnitTestCoverage = true
        }
        release {
            // Signed with Android Studio's debug key, so the APK installs on any phone without
            // a separate keystore. A store release would use its own key instead.
            signingConfig = signingConfigs.getByName("debug")
            // Code shrinking/optimization (R8) is off: with it on, the release build crashed on
            // start (IllegalAccessError in the Kotlin libraries). The APK stays small anyway.
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        // Lets java.time (LocalDate) work on API 24-25, below its native API 26.
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        viewBinding = true
    }
    androidResources {
        // Lists the app's languages (from values and values-bg) for Android 13+,
        // so the in-app language switch is accepted by the system.
        generateLocaleConfig = true
    }
}

// Room writes a JSON description of each database version here (needed for migrations).
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    // Fragments
    implementation(libs.androidx.fragment.ktx)

    // ViewModel + lifecycle (MVVM)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Navigation Component
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)

    // Room (local database)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Hilt (dependency injection)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}