import java.util.*

plugins {
    id("com.android.application")
    kotlin("android")
}

dependencies {
    implementation(project(":Branch-SDK"))
    implementation("com.google.android.gms:play-services-ads:16.0.0")

    /* Add chrome custom tabs for guaranteed matching */
    implementation("androidx.browser:browser:1.0.0") {
        exclude(module = "support-v4")
    }

    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("androidx.test:runner:1.2.0")
    androidTestImplementation("androidx.test:rules:1.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}

android {
    val ANDROID_BUILD_SDK_VERSION: String by project
    val ANDROID_BUILD_TARGET_SDK_MINIMUM: String by project
    val ANDROID_BUILD_TARGET_SDK_VERSION: String by project
    val VERSION_NAME: String by project
    val VERSION_CODE: String by project

    compileSdk = ANDROID_BUILD_SDK_VERSION.toInt()
    defaultConfig {
        applicationId = "io.branch.branchandroiddemo"
        minSdk = ANDROID_BUILD_TARGET_SDK_MINIMUM.toInt()
        targetSdk = ANDROID_BUILD_TARGET_SDK_VERSION.toInt()
        versionName = VERSION_NAME
        versionCode = VERSION_CODE.toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val propFile = file("signing.properties")
    val props = if (propFile.exists()) {
        val props = Properties()
        props.load(propFile.inputStream())
        props
    } else {
        null
    }

    if (props != null) {
        val pStoreFile = props["STORE_FILE"] as? String
        val pStorePassword = props["STORE_PASSWORD"] as? String
        val pKeyAlias = props["KEY_ALIAS"] as? String
        val pKeyPassword = props["KEY_PASSWORD"] as? String
        if (!pStoreFile.isNullOrBlank()
            && !pStorePassword.isNullOrBlank()
            && !pKeyAlias.isNullOrBlank()
            && !pKeyPassword.isNullOrBlank()
        ) {
            signingConfigs {
                create("release") {
                    storeFile = file(pStoreFile)
                    storePassword = pStorePassword
                    keyAlias = pKeyAlias
                    keyPassword = pKeyPassword
                }
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
        }
    }
}
