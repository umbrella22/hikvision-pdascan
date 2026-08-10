plugins {
    id("com.android.library")
}

android {
    namespace = "com.ikaros.hikvision.mdt201"
    compileSdk = 30

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    compileOnly(fileTree("libs") { include("uniapp-v8-release.aar") })
    implementation(libs.fastjson)
}
