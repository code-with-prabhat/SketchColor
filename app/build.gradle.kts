
plugins {
    id("com.android.application")

}

android {
    namespace = "com.sketchcolor"
    compileSdk = 33
    buildToolsVersion = "33.0.2"

    defaultConfig {
        applicationId = "com.sketchcolor"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        
        vectorDrawables { 
            useSupportLibrary = true
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildTypes {
        release {
            isMinifyEnabled=true
            isShrinkResources=true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":crashreporter"))
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation("com.google.android.material:material:1.5.0")
    
}
