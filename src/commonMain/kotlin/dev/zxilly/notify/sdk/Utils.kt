package dev.zxilly.notify.sdk

import io.ktor.client.statement.*
import io.ktor.http.*

val ok: HttpResponse.() -> Boolean = {
    val codes = setOf(
        HttpStatusCode.OK,
        HttpStatusCode.Created,
        HttpStatusCode.Accepted,
        HttpStatusCode.NoContent,
        HttpStatusCode.NotModified
    )
    codes.contains(status)
}

val emptyContentError = Error("Content is empty")

val isUnitTest by lazy {
    try {
        throw Error("test")
    } catch (e: Throwable) {
        return@lazy e.stackTraceToString()
            .split("\n")
            .any {
                it.contains("Test")
            }
    }
}

val defaultEndpoint: String
    get() {
        return if (isUnitTest) {
            "http://localhost:14444"
        } else {
            "https://push.learningman.top"
        }
    }

fun isUUID(str: String): Boolean {
    return str.matches(Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
}
