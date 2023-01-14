package dev.zxilly.notify.sdk.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Channel(val value: String) {
    @SerialName("FCM")
    FCM("FCM"),

    @SerialName("WebSocket")
    WebSocket("WebSocket"),

    @SerialName("WebPush")
    WebPush("WebPush"),

    @SerialName("WNS")
    WNS("WNS"),

    @SerialName("Telegram")
    Telegram("Telegram"),
}