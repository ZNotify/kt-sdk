import dev.zxilly.notify.sdk.defaultEndpoint
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.test.fail


object TestUtils {
    suspend fun check() {
        try {
            with(HttpClient(){
                install(UserAgent) {
                    agent = "Kotlin SDK test util"
                }
            }.get("$defaultEndpoint/alive")) {
                if (status != HttpStatusCode.NoContent) {
                    fail("Test server is not running")
                }
            }
        } catch (e: Exception) {
            fail("Test server is not running\n${e.stackTraceToString()}")
        }
    }
}