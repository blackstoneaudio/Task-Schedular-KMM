plugins {
    kotlin("multiplatform") version "1.5.30"
    id("com.android.library")
    id("kotlin-android-extensions")
    id("maven-publish")
}

group = "com.blackstone"
version = "0.1.0"

repositories {
    google()
    mavenCentral()
}

kotlin {
    val coroutines_version = "1.5.0-native-mt"
    android()
    iosX64("ios") {
        binaries {
            framework {
                baseName = "library"
            }
        }
    }

    jvm()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting
        val androidTest by getting
        val iosMain by getting
        val iosTest by getting
        val jvmTest by getting
    }

    tasks.withType(Test::class) {
        useJUnitPlatform()
        jvmArgs?.add("-Xopt-in=kotlin.RequiresOptIn")
    }

}

android {
    compileSdkVersion(30)
//    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(30)
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}