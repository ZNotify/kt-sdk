import io.ktor.client.statement.*

val ok: HttpResponse.() -> Boolean = {
    val codes = listOf(200, 201, 304)
    codes.contains(status.value)
}

val emptyContentError = Error("Content is empty")

val isTest by lazy {
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

fun getEndpoint(): String {
    return if (isTest) {
        "http://localhost:14444"
    } else {
        "https://push.learningman.top"
    }
}
