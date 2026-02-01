
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

fun getProperty(key: String, envVar: String): String {
    val keystoreFile = keystoreProperties[key]
    if(keystoreFile !is String) return System.getenv(envVar)
    return keystoreFile
}

android {
    namespace = "fr.focusphone"
    compileSdk = 36

    defaultConfig {
        applicationId = "fr.focusphone"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = System.getenv("APP_VERSION") ?: "debug"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("../release-keystore.jks") // Generated during the build the github action
            storePassword = getProperty("storePassword", "KEYSTORE_PASSWORD")
            keyAlias = getProperty("keyAlias", "KEY_ALIAS")
            keyPassword = getProperty("keyPassword", "KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
