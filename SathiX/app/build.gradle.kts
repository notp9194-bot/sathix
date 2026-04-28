import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs.kotlin")
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(FileInputStream(f))
}
fun prop(key: String, default: String = ""): String =
    localProps.getProperty(key) ?: System.getenv(key) ?: default

android {
    namespace = "com.sathix.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sathix.app"
        minSdk = 24
        targetSdk = 34
        versionCode = (System.getenv("VERSION_CODE")?.toIntOrNull()) ?: 1
        versionName = System.getenv("VERSION_NAME") ?: "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"${prop("CLOUDINARY_CLOUD_NAME", "dvqqgqdls")}\"")
        buildConfigField("String", "CLOUDINARY_API_KEY", "\"${prop("CLOUDINARY_API_KEY")}\"")
        buildConfigField("String", "CLOUDINARY_API_SECRET", "\"${prop("CLOUDINARY_API_SECRET")}\"")
        buildConfigField("String", "SIGNALING_SERVER_URL", "\"${prop("SIGNALING_SERVER_URL", "https://your-render-app.onrender.com")}\"")
        buildConfigField("String", "STUN_SERVER", "\"${prop("STUN_SERVER", "stun:stun.l.google.com:19302")}\"")
        buildConfigField("String", "TURN_SERVER", "\"${prop("TURN_SERVER", "")}\"")
        buildConfigField("String", "TURN_USERNAME", "\"${prop("TURN_USERNAME", "")}\"")
        buildConfigField("String", "TURN_PASSWORD", "\"${prop("TURN_PASSWORD", "")}\"")
    }

    signingConfigs {
        create("release") {
            val storeFilePath = System.getenv("KEYSTORE_PATH")
            if (!storeFilePath.isNullOrBlank() && file(storeFilePath).exists()) {
                storeFile = file(storeFilePath)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            val keystorePath = System.getenv("KEYSTORE_PATH")
            if (!keystorePath.isNullOrBlank() && file(keystorePath).exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += setOf("/META-INF/{AL2.0,LGPL2.1}", "META-INF/DEPENDENCIES")
        }
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.fragment:fragment-ktx:1.8.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    val lifecycle = "2.8.3"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle")
    implementation("androidx.lifecycle:lifecycle-service:$lifecycle")
    implementation("androidx.lifecycle:lifecycle-process:$lifecycle")

    val nav = "2.7.7"
    implementation("androidx.navigation:navigation-fragment-ktx:$nav")
    implementation("androidx.navigation:navigation-ui-ktx:$nav")

    val coroutines = "1.8.1"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$coroutines")

    val room = "2.6.1"
    implementation("androidx.room:room-runtime:$room")
    implementation("androidx.room:room-ktx:$room")
    ksp("androidx.room:room-compiler:$room")

    val hilt = "2.51.1"
    implementation("com.google.dagger:hilt-android:$hilt")
    ksp("com.google.dagger:hilt-android-compiler:$hilt")
    implementation("androidx.hilt:hilt-navigation-fragment:1.2.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    implementation("androidx.work:work-runtime-ktx:2.9.0")

    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")

    implementation("com.google.android.gms:play-services-auth:21.2.0")

    implementation("com.cloudinary:cloudinary-android:2.5.0")

    implementation("io.socket:socket.io-client:2.1.0") { exclude(group = "org.json", module = "json") }

    implementation("io.github.webrtc-sdk:android:114.5735.10")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    ksp("com.github.bumptech.glide:ksp:4.16.0")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("com.squareup.leakcanary:plumber-android:2.14")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")

    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("com.google.code.gson:gson:2.11.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
