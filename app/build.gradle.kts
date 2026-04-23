import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

// Upload keystore config — loaded from keystore.properties at the repo root of the
// Android submodule. That file is gitignored (see .gitignore: *.jks / keystore.properties).
// If the file is missing (fresh clone, CI without secrets), release builds fall back
// to the debug signing config so `assembleRelease` still produces an unsigned-for-Play
// AAB you can inspect locally. For real Play uploads you MUST have keystore.properties.
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps: Properties? = if (keystorePropsFile.exists()) {
    Properties().apply { keystorePropsFile.inputStream().use { load(it) } }
} else null

android {
    namespace = "com.teachmeski.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.teachmeski.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 6
        versionName = "1.0.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            debugSymbolLevel = "FULL"
        }

        buildConfigField("String", "SUPABASE_URL", "\"https://ifzqezqnvmthexbybldh.supabase.co\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlmenFlenFudm10aGV4YnlibGRoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzU5MTQ3NTgsImV4cCI6MjA5MTQ5MDc1OH0.nw0bDRSH-DF5Y3BK0gc3xXmeEeXOJakhIdpzBhisYu4\"")
    }

    signingConfigs {
        if (keystoreProps != null) {
            create("release") {
                val storeFilePath = keystoreProps.getProperty("storeFile")
                    ?: error("keystore.properties: missing storeFile")
                storeFile = rootProject.file(storeFilePath).let { f ->
                    if (f.exists()) f else file(storeFilePath)
                }
                storePassword = keystoreProps.getProperty("storePassword")
                    ?: error("keystore.properties: missing storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                    ?: error("keystore.properties: missing keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
                    ?: error("keystore.properties: missing keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (keystoreProps != null) {
                signingConfigs.getByName("release")
            } else {
                // Dev fallback: unsigned-for-Play. For a real Play upload you need
                // keystore.properties. See SIGNING.md.
                signingConfigs.getByName("debug")
            }
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
}

dependencies {
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    val supabaseBom = platform(libs.supabase.bom)
    implementation(supabaseBom)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.auth)
    implementation(libs.supabase.realtime)
    implementation(libs.supabase.storage)
    implementation(libs.supabase.functions)
    implementation(libs.ktor.client.okhttp)

    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.material)

    implementation(libs.play.billing)
    implementation(libs.androidx.browser)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.kotlinx.coroutines.play.services)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
