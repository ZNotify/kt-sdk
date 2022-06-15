import entity.Message
import entity.MessageItem
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

const val TEST_ENDPOINT = "http://localhost:14444"

@OptIn(DelicateCoroutinesApi::class)
@ExperimentalCoroutinesApi
class ClientTest {
    @BeforeTest
    fun testServer() = runTest {
        try {
            with(HttpClient().get("$TEST_ENDPOINT/alive")) {
                if (status != HttpStatusCode.NoContent) {
                    fail("Test server is not running")
                }
            }
        } catch (e: Exception) {
            fail("Test server is not running")
        }
    }

    @Test
    fun testCreate() = runTest {
        val ret = Client.create("test", TEST_ENDPOINT)
        assertTrue { ret.isSuccess }
        assertTrue { ret.getOrNull() is Client }
    }

    @Test
    fun testCreateFailed() = runTest {
        val ret = Client.create("error", TEST_ENDPOINT)
        assertTrue { ret.isFailure }
        assertTrue { ret.exceptionOrNull() is Throwable }
    }

    @Test
    fun checkSendMessage() = runTest {
        val client = Client.create("test", TEST_ENDPOINT).getOrNull()!!
        val ret = client.send(Message("test"))
        assertTrue { ret.isSuccess }
        assertTrue { ret.getOrNull() is MessageItem }
        assertEquals(ret.getOrNull()!!.content, "test")
    }

    @Test
    fun checkSendMessage2() = runTest {
        val client = Client.create("test", TEST_ENDPOINT).getOrNull()!!
        val ret = client.send(Message("test", "test_title"))
        assertTrue { ret.isSuccess }
        assertTrue { ret.getOrNull() is MessageItem }
        assertEquals(ret.getOrNull()!!.content, "test")
        assertEquals(ret.getOrNull()!!.title, "test_title")
    }

    @Test
    fun checkSendMessageFailed() = runTest {
        val client = Client.create("test", TEST_ENDPOINT).getOrNull()!!
        val ret = client.send(Message(""))
        assertTrue { ret.isFailure }
        assertTrue { ret.exceptionOrNull() is Throwable }
    }
}