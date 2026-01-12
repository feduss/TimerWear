plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.feduss.timerwear"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.feduss.timerwear"
        minSdk = 26
        targetSdk = 36
        versionCode = 40
        versionName = "1.1.4"

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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    //Modules
    implementation(project(":business"))
    implementation(project(":data"))
    implementation(project(":utils"))
    implementation(project(":entity"))
    implementation(project(":uistate"))

    //Kotlin core
    implementation("androidx.core:core-ktx:1.17.0")

    //Compose wear
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.activity:activity-compose:1.12.2")
    implementation(platform("androidx.compose:compose-bom:2025.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("com.google.android.material:material:1.13.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.12.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    //
    implementation("androidx.wear.compose:compose-foundation:1.5.6")
    implementation("androidx.wear.compose:compose-material:1.5.6")

    //
    implementation("com.github.nanihadesuka:LazyColumnScrollbar:2.2.0")
    implementation("com.github.anhaki:PickTime-Compose:1.1.5")

    //Hilt
    implementation("com.google.dagger:hilt-android:2.57.2")
    ksp("com.google.dagger:hilt-android-compiler:2.57.2")
    implementation("androidx.hilt:hilt-work:1.3.0")
    implementation("androidx.work:work-runtime-ktx:2.11.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")

    //Gson
    implementation("com.google.code.gson:gson:2.13.2")

    //Skeleton view
    implementation("com.valentinilk.shimmer:compose-shimmer:1.3.3")

    //APIs for SplashScreen, including compatibility helpers on devices prior Android 12
    implementation("androidx.core:core-splashscreen:1.2.0")

    //Reboot helper
    implementation("com.jakewharton:process-phoenix:3.0.0")

    //Tooltip
    implementation("com.github.skydoves:balloon-compose:1.7.2")

    //Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
}