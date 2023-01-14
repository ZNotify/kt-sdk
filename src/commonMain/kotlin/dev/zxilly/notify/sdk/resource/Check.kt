package dev.zxilly.notify.sdk.resource

import io.ktor.resources.*

@Suppress("PropertyName")
@Resource("/check")
data class Check(val user_secret: String)