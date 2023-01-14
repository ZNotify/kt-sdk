package dev.zxilly.notify.sdk.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Priority(val value: String) {
    @SerialName("high")
    High("high"),

    @SerialName("normal")
    Normal("normal"),

    @SerialName("low")
    Low("low"),
}
