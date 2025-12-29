import java.util.Properties
plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.android") version "1.9.0"
}
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}
android {
    namespace = "com.synguyen.se114project"
    compileSdk {
        version = release(36)
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    packaging {
        resources {
            excludes += "META-INF/*" // Tránh lỗi duplicate file khi build
        }
    }
    defaultConfig {
        applicationId = "com.synguyen.se114project"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SUPABASE_URL", localProperties.getProperty("SUPABASE_URL"))
        buildConfigField("String", "SUPABASE_KEY", localProperties.getProperty("SUPABASE_KEY"))
        // Optional: local Socket server URL for development (e.g., http://10.0.2.2:3000)
        buildConfigField("String", "SOCKET_SERVER_URL", localProperties.getProperty("SOCKET_SERVER_URL"))
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
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("com.google.android.material:material:1.12.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.room.common.jvm)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    val room_version = "2.8.4"

    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")

    // Thư viện để hiện dot cho lịch MaterialCalendarView
    implementation("com.applandeo:material-calendar-view:1.9.0-rc03")

    // Thư viện hỗ trợ LiveData và ViewModel cho Java
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.10.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.10.0")

    // THÊM SUPABASE SDK
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // Thêm Logging để xem API chạy thế nào (Debug cực tiện)
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Socket.IO client (Android)
    implementation("io.socket:socket.io-client:2.0.1") // uses engine.io-client and org.json
    implementation("org.json:json:20230227")

    implementation("androidx.work:work-runtime:2.9.0")
}