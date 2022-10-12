import dev.zxilly.notify.sdk.send
import dev.zxilly.notify.sdk.entity.MessagePayload
import dev.zxilly.notify.sdk.entity.MessageItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SendTest {
    @BeforeTest
    fun testServer() = runTest {
        TestUtils.check()
    }

    @Test
    fun testCreateFailed() = runTest {
        val ret = send("error", "test")
        assertTrue { ret.isFailure }
        assertTrue { ret.exceptionOrNull() is Throwable }
    }

    @Test
    fun checkSendMessage() = runTest {
        val ret = send("test", MessagePayload("test"))
        assertTrue { ret.isSuccess }
        assertTrue { ret.getOrNull() is MessageItem }
        assertEquals(ret.getOrNull()!!.content, "test")
    }

    @Test
    fun checkSendMessage2() = runTest {
        val ret = send("test", MessagePayload("test", "test_title"))
        assertTrue { ret.isSuccess }
        assertTrue { ret.getOrNull() is MessageItem }
        assertEquals(ret.getOrNull()!!.content, "test")
        assertEquals(ret.getOrNull()!!.title, "test_title")
    }

    @Test
    fun checkSendMessageFailed() = runTest {
        val ret = send("test", MessagePayload(""))
        assertTrue { ret.isFailure }
        assertTrue { ret.exceptionOrNull() is Throwable }
    }
}