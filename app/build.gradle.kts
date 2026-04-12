import org.gradle.kotlin.dsl.invoke

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.qr_scanner_tsd"
    
    buildFeatures {
        viewBinding = true
    }

    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.qr_scanner_tsd"
        minSdk = 24
        targetSdk = 36

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = true
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}
dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    implementation(files("libs/ru.atol.barcodeservice.api-release-1.5.32.aar"))

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.fragment)
    implementation(libs.drawerlayout)
    implementation(libs.poi)
    implementation(libs.poi.ooxml)

    // Force log4j-api 2.17.1 to avoid MethodHandle issues on API < 26
    implementation("org.apache.logging.log4j:log4j-api") {
        version {
            strictly("2.17.1")
        }
    }

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}