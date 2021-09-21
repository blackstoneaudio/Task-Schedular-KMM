plugins {
    kotlin("multiplatform") version "1.5.30"
//    id("com.android.library")
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
//    android()
    iosX64("ios") {
        binaries {
            framework {
                baseName = "TaskScheduler"
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

    }

    tasks.withType(Test::class) {
        useJUnitPlatform()
        jvmArgs?.add("-Xopt-in=kotlin.RequiresOptIn")
    }

}

// I don't think this is needed since we have a jvm target... pretty sure it's
// only needed if you are putting resources or something in an .aar file
//android {
//    compileSdkVersion(30)
////    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
//    defaultConfig {
//        minSdkVersion(24)
//        targetSdkVersion(30)
//    }
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_1_8
//        targetCompatibility = JavaVersion.VERSION_1_8
//    }
//}


publishing {
    // this fetches our credentials from ~/.gradle/gradle.properties
    val mavenUser: String by project
    val mavenPassword: String by project

    repositories {
        maven {
            setUrl("https://repos.awhb.dev/releases")
            authentication {
                create("basic", BasicAuthentication::class.java)
            }
            credentials {
                username = mavenUser
                password = mavenPassword
            }
        }
    }
}

