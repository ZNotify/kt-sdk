import io.ktor.client.statement.*

val ok: HttpResponse.() -> Boolean = {
    val codes = listOf(200, 201, 304)
    codes.contains(status.value)
}

val emptyContentError = Error("Content is empty")