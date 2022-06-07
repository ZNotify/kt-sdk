import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@ExperimentalCoroutinesApi
class ClientTest {
    @Test
    fun testSend() = runTest {
        val client = Client.create("zxilly")
    }
}