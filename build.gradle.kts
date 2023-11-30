@file:Suppress("UnstableApiUsage")

import com.codingfeline.buildkonfig.compiler.FieldSpec

group = "dev.zxilly"

fun isCI() = System.getenv("CI") != null
fun isPublish() = gradle.startParameter.taskNames.any { it.contains("publish") }

repositories {
    google()
    mavenCentral()
}

plugins {
    val ktVersion = "1.9.10"

    kotlin("multiplatform") version ktVersion
    kotlin("plugin.serialization") version ktVersion

    id("com.android.library") version "8.1.4"
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("com.dorongold.task-tree") version "2.1.1"
    id("com.codingfeline.buildkonfig") version "0.13.3"
    id("dev.zxilly.gradle.keeper") version "0.0.5"

    id("maven-publish")
    id("signing")
}

keeper {
    expectValue = isPublish()

    environment(true)

    if (!isCI()) {
        properties()
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }

        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    js {
        nodejs()
        browser()
    }

    androidTarget {
        publishLibraryVariants("release", "debug")
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    linuxX64()
    macosX64()
    mingwX64()

    sourceSets {
        val ktorVersion = "2.3.6"
        val serializationVersion = "1.5.1"
        val coroutinesVersion = "1.7.3"

        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-client-resources:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-java:$ktorVersion")
            }
        }
        val macosX64Main by getting {
            dependencies {
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-js:$ktorVersion")
            }
        }
        val linuxX64Main by getting {
            dependencies {
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-android:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
            }
        }
        val mingwX64Main by getting {
            dependencies {
                implementation("io.ktor:ktor-client-winhttp:$ktorVersion")
            }
        }
    }
}

if (isCI()) {
    when (System.getenv("GITHUB_EVENT_NAME")) {
        "release" -> {
            val tag = System.getenv("GITHUB_REF_NAME")
            if (tag.isNullOrBlank()) {
                throw IllegalArgumentException("GITHUB_REF_NAME is not set")
            }
            version = tag
            logger.info("Release: $tag")
        }

        "push" -> {
            val commit = System.getenv("GITHUB_SHA")
            if (commit.isNullOrBlank()) {
                throw IllegalArgumentException("GITHUB_SHA is not set")
            }
            val shortSHA = commit.substring(0, 7)
            version = "master-$shortSHA-SNAPSHOT"
        }
    }
} else {
    version = secret.get("library.version") ?: "local"
}

buildkonfig {
    packageName = "dev.zxilly.notify.sdk"

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "VERSION", version.toString())
    }
}

android {
    compileSdk = 33
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    namespace = "dev.zxilly.notify.sdk"
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    publishing {
        multipleVariants {
            allVariants()
            withJavadocJar()
            withSourcesJar()
        }
    }
}

val mavenCentralUser = secret.get("maven.user")
val mavenCentralPassword = secret.get("maven.password")

val githubUser = secret.get("github.user")
val githubToken = secret.get("github.token")

val githubPackageRegistryUrl = uri("https://maven.pkg.github.com/ZNotify/kt-sdk")

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(mavenCentralUser)
            password.set(mavenCentralPassword)
            packageGroup.set("dev.zxilly")
        }
    }
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing {
    repositories {
        maven {
            name = "github"
            url = githubPackageRegistryUrl
            credentials {
                username = githubUser
                password = githubToken
            }
        }
        maven {
            name = "local"
            url = uri("build/repo")
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
    val signingKey = secret.getBase64("signing.key")
    val signingPassword = secret.get("signing.password")
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
}