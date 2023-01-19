package dev.zxilly.notify.sdk.internal

import dev.zxilly.notify.sdk.BuildConfig

internal const val buildVariant = BuildConfig.BUILD_TYPE

actual val currentPlatform = "android-$buildVariant"