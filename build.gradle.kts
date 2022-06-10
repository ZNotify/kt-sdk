@file:Suppress("UnstableApiUsage")

import java.io.File
import java.io.FileInputStream
import java.util.*

group = "dev.zxilly"
version = "1.0"

repositories {
    google()
    mavenCentral()
}

plugins {
    kotlin("multiplatform") version "1.7.0"
    kotlin("plugin.serialization") version "1.7.0"
    id("com.android.library")
    id("maven-publish")
    id("signing")
}

kotlin {
    sourceSets.all {
        languageSettings.apply {
            languageVersion = "1.7"
            apiVersion = "1.7"
        }
    }

    targets {
        jvm {
            compilations.all {
                kotlinOptions.jvmTarget = "1.8"
            }
            testRuns["test"].executionTask.configure {
                useJUnitPlatform()
            }
        }

        android("android") {
            publishLibraryVariants("release", "debug")
        }
    }

    sourceSets {
        val ktorVersion = "2.0.2"
        val serializationVersion = "1.3.3"

        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.2")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
            }
        }
        val androidMain by getting {
            dependencies {
                rootProject
                implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
            }
        }
    }
}

tasks.forEach {
    if (it.name.startsWith("lint")) {
        it.enabled = false
    }
}

android {
    compileSdk = 30
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 24
        targetSdk = 32
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    lint {
        isCheckTestSources = false
        isCheckReleaseBuilds = false
        isAbortOnError = false
    }

    buildToolsVersion = "30.0.3"
}

fun String.mapToEnv(): String {
    return this.toUpperCase().replace(".", "_")
}

val props = Properties().apply {
    val file = File(rootProject.rootDir, "local.properties")
    if (file.exists()){
        load(FileInputStream(file))
    }
}

fun getKey(key: String, base64:Boolean = false): String {
    val value = props.getProperty(key) ?: (System.getenv(key.mapToEnv())
        ?: throw IllegalArgumentException("$key is not defined"))
    return if (base64){
        // decode base64
        Base64.getDecoder().decode(value).toString(Charsets.UTF_8)
    } else {
        value
    }
}

val mavenUser = getKey("maven.user")
val mavenPassword = getKey("maven.password")

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing {
    repositories {
        maven {
            name = "sonatype"
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            credentials {
                username = mavenUser
                password = mavenPassword
            }
        }
    }

    publications.withType<MavenPublication> {
        artifact(javadocJar.get())

        pom {
            name.set("ZNotify Kotlin SDK")
            description.set("Kotlin multi platform sdk for ZNotify")
            url.set("https://github.com/ZNotify/kt-sdk")

            licenses {
                license {
                    name.set("Apache-2.0")
                    url.set("https://opensource.org/licenses/Apache-2.0")
                }
            }
            developers {
                developer {
                    id.set("Zxilly")
                    name.set("Zxilly")
                    email.set("zxilly@outlook.com")
                }
            }
            scm {
                url.set("https://github.com/ZNotify/kt-sdk.git")
            }

        }
    }
}

signing {
    val signingKey = getKey("signing.key", true)
    val signingPassword = getKey("signing.password")
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
}