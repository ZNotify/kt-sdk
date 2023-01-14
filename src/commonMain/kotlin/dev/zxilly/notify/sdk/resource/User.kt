package dev.zxilly.notify.sdk.resource

import io.ktor.resources.*

@Resource("/{secret}")
data class User(val secret: String) {

    @Resource("/device/{deviceID}")
    data class Device(val parent: User, val deviceID: String)

    @Resource("/devices")
    data class Devices(val parent: User)

    @Resource("/message/{messageID}")
    data class Message(val parent: User, val messageID: String)

    @Resource("/messages")
    data class Messages(val parent: User)

    @Resource("/send")
    data class Send(val parent: User)
}
