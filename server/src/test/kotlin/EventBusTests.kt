import com.jetbrains.teamcity.plugins.unrealengine.server.EventBus
import com.jetbrains.teamcity.plugins.unrealengine.server.EventBusConfig
import com.jetbrains.teamcity.plugins.unrealengine.server.EventBusConsumer
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.mockk.Runs
import io.mockk.andThenJust
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventBusTests {
    private data class TestEvent(
        val data: String,
    )

    private val consumer = mockk<EventBusConsumer<TestEvent>>()

    @BeforeEach
    fun init() {
        clearAllMocks()

        coEvery { consumer.consume(any()) } coAnswers {
            delay(100)
        } andThenJust Runs
    }

    @Test
    fun `maintains the order of event processing`() =
        runTest {
            val eventBus = createBus()

            val firstEvent = TestEvent("some payload")
            val secondEvent = TestEvent("another payload")

            eventBus.dispatch(firstEvent)
            eventBus.dispatch(secondEvent)

            advanceTimeBy(50)
            coVerify(exactly = 1) { consumer.consume(match { it.data == firstEvent.data }) }
            confirmVerified(consumer)

            advanceTimeBy(51)
            coVerify(exactly = 1) { consumer.consume(match { it.data == secondEvent.data }) }
            confirmVerified(consumer)
        }

    @Test
    fun `notifies when internal buffer overflows`() =
        runTest {
            val droppedElements = mutableListOf<TestEvent>()
            val eventBus =
                createBus(
                    bufferSize = 2,
                ) {
                    droppedElements.add(it)
                }

            (0..4).forEach {
                eventBus.dispatch(TestEvent("payload $it"))
                advanceTimeBy(5)
            }
            advanceUntilIdle()

            droppedElements.shouldNotBeEmpty()
            droppedElements.shouldHaveSize(2)
            droppedElements.shouldContainExactlyInAnyOrder(
                TestEvent("payload 1"),
                TestEvent("payload 2"),
            )
        }

    private fun TestScope.createBus(
        bufferSize: Int = 2,
        onBufferOverflow: ((TestEvent) -> Unit)? = null,
    ) = EventBus(
        EventBusConfig(
            "Test Bus",
            bufferSize,
        ),
        backgroundScope,
        listOf(consumer),
        onBufferOverflow = onBufferOverflow,
    )
}
