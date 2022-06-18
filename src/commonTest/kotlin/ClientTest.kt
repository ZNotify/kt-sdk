import entity.Message
import entity.MessageItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.*

@ExperimentalCoroutinesApi
class ClientTest {
    @BeforeTest
    fun testServer() = runTest {
        TestUtils.check()
    }

    @Test
    fun testCreate() = runTest {
        val ret = Client.create("test")
        assertTrue { ret.isSuccess }
        assertTrue { ret.getOrNull() is Client }
    }

    @Test
    fun testCreateFailed() = runTest {
        val ret = Client.create("error")
        assertTrue { ret.isFailure }
        assertTrue { ret.exceptionOrNull() is Throwable }
    }

    @Test
    fun checkSendMessage() = runTest {
        val client = Client.create("test").getOrElse {
            fail(it.stackTraceToString())
        }
        val ret = client.send(Message("test"))
        assertTrue { ret.isSuccess }
        assertTrue { ret.getOrNull() is MessageItem }
        assertEquals(ret.getOrNull()!!.content, "test")
    }

    @Test
    fun checkSendMessage2() = runTest {
        val client = Client.create("test").getOrElse {
            fail(it.stackTraceToString())
        }
        val ret = client.send(Message("test", "test_title"))
        assertTrue { ret.isSuccess }
        assertTrue { ret.getOrNull() is MessageItem }
        assertEquals(ret.getOrNull()!!.content, "test")
        assertEquals(ret.getOrNull()!!.title, "test_title")
    }

    @Test
    fun checkSendMessageFailed() = runTest {
        val client = Client.create("test").getOrElse {
            fail(it.stackTraceToString())
        }
        val ret = client.send(Message(""))
        assertTrue { ret.isFailure }
        assertTrue { ret.exceptionOrNull() is Throwable }
    }
}