pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == "com.android" || requested.id.name == "kotlin-android") {
                useModule("com.android.tools.build:gradle:4.2.2")
            }
        }
    }
}
rootProject.name = "notify-kt-sdk"

