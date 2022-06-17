import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.test.fail

suspend fun check() {
    try {
        with(HttpClient().get("${getEndpoint()}/alive")) {
            if (status != HttpStatusCode.NoContent) {
                fail("Test server is not running")
            }
        }
    } catch (e: Exception) {
        fail("Test server is not running")
    }
}