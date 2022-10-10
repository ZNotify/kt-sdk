pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == "com.android" || requested.id.name == "kotlin-android") {
                useModule("com.android.tools.build:gradle:7.3.0")
            }
        }
    }
}
rootProject.name = "notify-sdk"

