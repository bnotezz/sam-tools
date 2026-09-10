import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val versionPropsFile = rootProject.file("version.properties")
val versionProps = Properties().apply {
    if (versionPropsFile.exists()) {
        FileInputStream(versionPropsFile).use { load(it) }
    }
}

val versionMajor = versionProps.getProperty("major", "1").toInt()
val versionMinor = versionProps.getProperty("minor", "0").toInt()
val versionPatch = versionProps.getProperty("patch", "0").toInt()

val calculatedVersionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch
val calculatedVersionName = "$versionMajor.$versionMinor.$versionPatch"

android {
    namespace = "com.samtools"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.samtools"
        minSdk = 26
        targetSdk = 35
        versionCode = calculatedVersionCode
        versionName = calculatedVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_FILE")
            val storePass = System.getenv("KEYSTORE_PASSWORD")
            val keyUsr = System.getenv("KEY_ALIAS")
            val keyPass = System.getenv("KEY_PASSWORD")

            if (!keystorePath.isNullOrBlank() && file(keystorePath).exists()) {
                storeFile = file(keystorePath)
                storePassword = storePass
                keyAlias = keyUsr
                keyPassword = keyPass
            } else {
                // Fallback to debug keys if no production keystore is provided
                initWith(getByName("debug"))
            }
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
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

// --- Semantic Versioning Helper Tasks ---

tasks.register("printVersion") {
    group = "versioning"
    description = "Prints current semantic version details"
    doLast {
        println("versionName: $calculatedVersionName")
        println("versionCode: $calculatedVersionCode")
        println("tag: v$calculatedVersionName")
    }
}

tasks.register("bumpPatch") {
    group = "versioning"
    description = "Increments patch version in version.properties"
    doLast {
        val props = Properties()
        if (versionPropsFile.exists()) {
            FileInputStream(versionPropsFile).use { props.load(it) }
        }
        val currentPatch = props.getProperty("patch", "0").toInt()
        val nextPatch = currentPatch + 1
        props.setProperty("patch", nextPatch.toString())
        FileOutputStream(versionPropsFile).use {
            props.store(it, "Auto-incremented by Gradle bumpPatch task")
        }
        val maj = props.getProperty("major", "1")
        val min = props.getProperty("minor", "0")
        println("Bumped patch version: $maj.$min.$nextPatch (Code: ${maj.toInt() * 10000 + min.toInt() * 100 + nextPatch})")
    }
}

tasks.register("bumpMinor") {
    group = "versioning"
    description = "Increments minor version and resets patch to 0 in version.properties"
    doLast {
        val props = Properties()
        if (versionPropsFile.exists()) {
            FileInputStream(versionPropsFile).use { props.load(it) }
        }
        val currentMinor = props.getProperty("minor", "0").toInt()
        val nextMinor = currentMinor + 1
        props.setProperty("minor", nextMinor.toString())
        props.setProperty("patch", "0")
        FileOutputStream(versionPropsFile).use {
            props.store(it, "Updated by Gradle bumpMinor task")
        }
        val maj = props.getProperty("major", "1")
        println("Bumped minor version: $maj.$nextMinor.0 (Code: ${maj.toInt() * 10000 + nextMinor * 100})")
    }
}

tasks.register("bumpMajor") {
    group = "versioning"
    description = "Increments major version and resets minor & patch to 0 in version.properties"
    doLast {
        val props = Properties()
        if (versionPropsFile.exists()) {
            FileInputStream(versionPropsFile).use { props.load(it) }
        }
        val currentMajor = props.getProperty("major", "1").toInt()
        val nextMajor = currentMajor + 1
        props.setProperty("major", nextMajor.toString())
        props.setProperty("minor", "0")
        props.setProperty("patch", "0")
        FileOutputStream(versionPropsFile).use {
            props.store(it, "Updated by Gradle bumpMajor task")
        }
        println("Bumped major version: $nextMajor.0.0 (Code: ${nextMajor * 10000})")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
