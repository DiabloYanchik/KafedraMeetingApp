plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.kafedrameetingapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.kafedrameetingapp"
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
    packaging {
        resources {
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    // JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // Firebase
    implementation(libs.firebase.database)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.auth)

    // Work
    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.core)

    // CardView
    implementation(libs.cardview)
    implementation(libs.recyclerview)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}