import groovy.json.JsonSlurper
import java.util.Base64

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.roborazzi)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

val logger = project.logger
var secrets: Map<String, Any>? = null

val json = System.getenv("PHOTO_RESIZER_SECRETS")

if (json.isNullOrEmpty()) {
    logger.warn("⚠ env not found. Using local project files if available.")
} else {
    try {
        logger.lifecycle("Parsing env...")

        @Suppress("UNCHECKED_CAST")
        secrets = JsonSlurper().parseText(json) as? Map<String, Any>

        val keystoreDir = file("keystore")
        keystoreDir.mkdirs()

        val googleServices = file("google-services.json")
        val googleServicesBase64 = secrets?.get("GOOGLE_SERVICES_JSON_BASE64") as? String
        if (googleServicesBase64 != null) {
            googleServices.writeBytes(Base64.getDecoder().decode(googleServicesBase64))
            logger.lifecycle("google-services.json restored.")
        }

        val keystore = file("keystore/releasekey.jks")
        val keystoreBase64 = secrets?.get("RELEASE_KEYSTORE_BASE64") as? String
        if (keystoreBase64 != null) {
            keystore.writeBytes(Base64.getDecoder().decode(keystoreBase64))
            logger.lifecycle("releasekey.jks restored.")
        }
    } catch (e: Exception) {
        logger.error("Failed to restore secrets.", e)
        throw e
    }
}

android {
    namespace = "com.shamim.photoresizer"

    // Standardized for API 36
    compileSdk = 36

    defaultConfig {
        applicationId = "com.shamim.photoresizer"
        minSdk = 24
        targetSdk = 36
        versionCode = 10
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val s = secrets
            if (s != null) {
                logger.lifecycle("Configuring release signing from env...")

                storeFile = file(s["STORE_FILE"] as String)
                storePassword = s["STORE_PASSWORD"] as String
                keyAlias = s["KEY_ALIAS"] as String
                keyPassword = s["KEY_PASSWORD"] as String

                logger.lifecycle("Release signing configured.")
            } else {
                logger.warn("⚠ No signing configuration found.")
            }
        }
    }

    buildTypes {
        release {
            isCrunchPngs = false
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    // implementation(platform(libs.firebase.bom))
    // implementation(libs.accompanist.permissions)
    implementation(libs.androidx.activity.compose)
    // implementation(libs.androidx.camera.camera2)
    // implementation(libs.androidx.camera.core)
    // implementation(libs.androidx.camera.lifecycle)
    // implementation(libs.androidx.camera.view)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    // implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.coil.compose)
    implementation(libs.converter.moshi)
    // implementation(libs.firebase.ai)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.logging.interceptor)
    implementation(libs.moshi.kotlin)
    implementation(libs.okhttp)
    // implementation(libs.play.services.location)
    implementation(libs.retrofit)
    implementation(libs.play.app.update)
    implementation(libs.play.app.update.ktx)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.fragment.ktx)

    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.core)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.runner)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    ksp(libs.androidx.room.compiler)
    ksp(libs.moshi.kotlin.codegen)

    // For Firebase analysis
    implementation(platform("com.google.firebase:firebase-bom:34.15.0"))
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
}
