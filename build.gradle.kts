@file:Suppress("UnstableApiUsage", "UNUSED_VARIABLE")

import com.codingfeline.buildkonfig.compiler.FieldSpec

group = "dev.zxilly"

//val props: Properties = Properties().apply {
//    val file = File(rootProject.rootDir, "local.properties")
//    if (file.exists()) {
//        load(FileInputStream(file))
//    }
//}

fun isCI() = System.getenv("CI") != null
fun isPublish() = gradle.startParameter.taskNames.any { it.contains("publish") }

repositories {
    google()
    mavenCentral()
}

//afterEvaluate {
//    // Remove log pollution until Android support in KMP improves.
//    project.extensions.findByType<KotlinMultiplatformExtension>()?.let { kmpExt ->
//        val sourceSetsToRemove = setOf(
//            "androidTestFixtures",
//            "androidTestFixturesDebug",
//            "androidTestFixturesRelease",
//            "androidAndroidTestRelease"
//        )
//        kmpExt.sourceSets.removeAll {
//            sourceSetsToRemove.contains(it.name)
//        }
//    }
//}

plugins {
    kotlin("multiplatform") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"

    id("com.android.library") version "7.4.0"
    id("io.codearte.nexus-staging") version "0.30.0"
    id("com.dorongold.task-tree") version "2.1.0"
    id("com.codingfeline.buildkonfig") version "0.13.3"
    id("dev.zxilly.gradle.keeper") version "0.0.5"

    id("maven-publish")
    id("signing")
}

keeper {
    expectValue = isPublish()

    environment(true)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }

    targets {
        jvm {
            testRuns["test"].executionTask.configure {
                useJUnitPlatform()
            }
        }
        js(IR) {
            nodejs()
            browser()
        }

        android("android") {
            publishLibraryVariants("release", "debug")
        }
        linuxX64()
        macosX64()
        mingwX64()
    }

    sourceSets {
        val ktorVersion = "2.2.2"
        val serializationVersion = "1.4.1"
        val coroutinesVersion = "1.6.4"

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
                implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
            }
        }
        val macosX64Main by getting {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:$ktorVersion")
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
                implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
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
    version = secret.get("library.version") ?: "SNAPSHOT"
}

buildkonfig {
    packageName = "dev.zxilly.notify.sdk"

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "VERSION", version.toString())
    }
}

//val hostOs: String = System.getProperty("os.name")
//val isMacosX64 = hostOs == "Mac OS X"
//val isLinuxX64 = hostOs == "Linux"
//
//fun tryDisableMacosNative(task: Task) {
//    if (task.name.contains("macos", true)) {
//        task.enabled = false
//        task.onlyIf { false }
//    }
//}
//
//fun tryDisableLinuxNative(task: Task) {
//    if (task.name.contains("linux", true)) {
//        task.enabled = false
//        task.onlyIf { false }
//    }
//}

//project.gradle.taskGraph.whenReady {
//    project.tasks.forEach {
//        if (it.name.contains("lint")) {
//            it.enabled = false
//        }
//        if (System.getenv("TEST") != null) {
//            if (isMacosX64) {
//                tryDisableLinuxNative(it)
//            } else if (isLinuxX64) {
//                tryDisableMacosNative(it)
//            }
//        }
//    }
//}

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
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
        singleVariant("debug") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

//fun String.mapToEnv(): String {
//    return this.toUpperCase().replace(".", "_")
//}

val mavenCentralUser = secret.get("maven.user")
val mavenCentralPassword = secret.get("maven.password")
val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")

val mavenCentralReleaseUrl =
    if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl

val githubUser = secret.get("github.user")
val githubToken = secret.get("github.token")

val githubPackageRegistryUrl = uri("https://maven.pkg.github.com/ZNotify/kt-sdk")

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

nexusStaging {
    serverUrl = "https://s01.oss.sonatype.org/service/local/"
    username = mavenCentralUser
    password = mavenCentralPassword
    packageGroup = "dev.zxilly"
    stagingProfileId = "95214448af0738"
}

publishing {
    repositories {
        maven {
            name = "sonatype"
            url = mavenCentralReleaseUrl
            credentials {
                username = mavenCentralUser
                password = mavenCentralPassword
            }
        }
        maven {
            name = "github"
            url = githubPackageRegistryUrl
            credentials {
                username = githubUser
                password = githubToken
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
    val signingKey = secret.getBase64("signing.key")
    val signingPassword = secret.get("signing.password")
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
}