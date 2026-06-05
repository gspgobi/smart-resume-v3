import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

val keystoreProps = Properties()
val keystoreFile = rootProject.file("keystore.properties")
if (keystoreFile.exists()) keystoreFile.inputStream().use { keystoreProps.load(it) }

android {
    namespace = "com.nithra.nithraresume"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.nithra.nithraresume"
        minSdk = 24
        targetSdk = 37
        versionCode = 73
        versionName = "4.2.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = keystoreProps["storeFile"]?.toString()?.let { rootProject.file(it) }
            storePassword = keystoreProps["storePassword"]?.toString() ?: ""
            keyAlias = keystoreProps["keyAlias"]?.toString() ?: ""
            keyPassword = keystoreProps["keyPassword"]?.toString() ?: ""
        }
    }

    buildTypes {
        debug {
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk.debugSymbolLevel = "FULL"
            signingConfig = signingConfigs.getByName("release")
        }
    }

    flavorDimensions += "environment"

    productFlavors {
        create("TestAdMob") {
            isDefault = true
            dimension = "environment"
            buildConfigField("Boolean", "isAdMobEnable", "true")
            buildConfigField("Boolean", "isTestAdMobId", "true")
        }
        create("ProdAdMob") {
            dimension = "environment"
            buildConfigField("Boolean", "isAdMobEnable", "true")
            buildConfigField("Boolean", "isTestAdMobId", "false")
        }
        create("NoAdMob") {
            dimension = "environment"
            buildConfigField("Boolean", "isAdMobEnable", "false")
            buildConfigField("Boolean", "isTestAdMobId", "true")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/LICENSE.md"
        }
    }
}

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            output.outputFileName.set(
                "smart-resume-${variant.flavorName}-${variant.buildType}-${output.versionCode.getOrElse(0)}-${output.versionName.getOrElse("")}.apk"
            )
        }
    }
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Compose BOM + UI
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    // Image loading
    implementation(libs.coil.compose)
    implementation(libs.android.image.cropper)

    // PDF generation
    implementation(libs.itextpdf)

    // Firebase BOM + libraries
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    // AdMob
    implementation(libs.play.services.ads)

    // In-App Review
    implementation(libs.play.review)

    // In-App Update
    implementation(libs.play.app.update)

    // Gson
    implementation(libs.gson)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Splash Screen
    implementation(libs.androidx.core.splashscreen)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

