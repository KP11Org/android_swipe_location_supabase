plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.swipeloacationcamera"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.swipeloacationcamera"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    //map
    implementation(libs.osmdroid.android)
    implementation("androidx.preference:preference:1.2.1")
    //location
    implementation(libs.play.services.location)
    //supabase
    implementation("io.github.jan-tennert.supabase:postgrest-kt:3.0.0")
    implementation("io.github.jan-tennert.supabase:auth-kt:3.0.0")
    implementation("io.ktor:ktor-client-okhttp:3.0.0")
    //default
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    //camera
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    //tests
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}