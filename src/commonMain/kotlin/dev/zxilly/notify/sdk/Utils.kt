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
            "http://127.0.0.1:14444"
        } else {
            "https://push.learningman.top"
        }
    }

fun String.removeSuffixSlash(): String {
    return if (this.endsWith("/")) {
        this.substring(0, this.length - 1)
    } else {
        this
    }
}

fun isUUID(str: String): Boolean {
    return str.matches(Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
}
