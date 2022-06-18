@file:Suppress("UnstableApiUsage", "UNUSED_VARIABLE")

import java.io.FileInputStream
import java.util.*

group = "dev.zxilly"
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
    }
} else {
    version = getKey("library.version", strict = true)
}

repositories {
    google()
    mavenCentral()
}

plugins {
    kotlin("multiplatform") version "1.7.0"
    kotlin("plugin.serialization") version "1.7.0"
    id("io.codearte.nexus-staging") version "0.30.0"
    id("com.dorongold.task-tree") version "2.1.0"
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
        js(BOTH) {
            nodejs()
            browser {
                commonWebpackConfig {
                    cssSupport.enabled = true
                }
            }
        }

        android("android") {
            publishLibraryVariants("release", "debug")
        }
        linuxX64()
        macosX64()
        mingwX64()
    }

    sourceSets {
        val ktorVersion = "2.0.2"
        val serializationVersion = "1.3.3"
        val coroutinesVersion = "1.6.1"

        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
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
        val mingwX64Main by getting {
            dependencies {
                implementation("io.ktor:ktor-client-curl:$ktorVersion")
            }
        }
        val macosX64Main by getting {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:$ktorVersion")
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
    }
}

val hostOs = System.getProperty("os.name")
val isMingwX64 = hostOs.startsWith("Windows")
val isMacosX64 = hostOs == "Mac OS X"
val isLinuxX64 = hostOs == "Linux"

fun tryDisableMingwNative(task: Task) {
    if (task.name.contains("mingw", true)) {
        task.enabled = false
        task.onlyIf { false }
    }
}

fun tryDisableMacosNative(task: Task) {
    if (task.name.contains("macos", true)) {
        task.enabled = false
        task.onlyIf { false }
    }
}

fun tryDisableLinuxNative(task: Task) {
    if (task.name.contains("linux", true)) {
        task.enabled = false
        task.onlyIf { false }
    }
}

project.gradle.taskGraph.whenReady {
    project.tasks.forEach {
        if (it.name.contains("lint")) {
            it.enabled = false
        }
        if (System.getenv("TEST") != null) {
            if (isMingwX64) {
                tryDisableLinuxNative(it)
                tryDisableMacosNative(it)
            } else if (isMacosX64) {
                tryDisableLinuxNative(it)
                tryDisableMingwNative(it)
            } else if (isLinuxX64) {
                tryDisableMacosNative(it)
                tryDisableMingwNative(it)
            }
        }
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
        isIgnoreTestSources = true
        isCheckTestSources = false
        isCheckReleaseBuilds = false
        isAbortOnError = false
    }

    buildToolsVersion = "30.0.3"
}

fun String.mapToEnv(): String {
    return this.toUpperCase().replace(".", "_")
}

lateinit var propsCache: Properties
val props: Properties
    get() {
        if (!::propsCache.isInitialized) {
            propsCache = Properties().apply {
                val file = File(rootProject.rootDir, "local.properties")
                if (file.exists()) {
                    load(FileInputStream(file))
                }
            }
        }
        return propsCache
    }

fun isCI() = System.getenv("CI") != null

fun getKey(key: String, base64: Boolean = false, strict: Boolean = false): String {
    var value: String? = if (isCI()) {
        System.getenv(key.mapToEnv())
    } else {
        props.getProperty(key)
    }
    if (value == null) {
        val warn = "$key is not defined"
        if (strict) {
            throw GradleException(warn)
        } else {
            logger.warn(warn)
            value = ""
        }
    }
    return if (base64) {
        // decode base64
        Base64.getDecoder().decode(value).toString(Charsets.UTF_8)
    } else {
        value
    }
}

val mavenUser = getKey("maven.user")
val mavenPassword = getKey("maven.password")
val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")

val releaseUrl = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

nexusStaging {
    serverUrl = "https://s01.oss.sonatype.org/service/local/"
    username = mavenUser
    password = mavenPassword
}

publishing {
    repositories {
        maven {
            name = "sonatype"
            url = releaseUrl
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