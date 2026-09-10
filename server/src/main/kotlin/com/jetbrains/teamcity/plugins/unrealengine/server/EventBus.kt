package com.jetbrains.teamcity.plugins.unrealengine.server

import com.jetbrains.teamcity.plugins.unrealengine.common.UnrealPluginLoggers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

data class EventBusConfig(
    val name: String,
    val bufferSize: Int,
)

fun interface EventBusConsumer<T> {
    suspend fun consume(event: T)
}

class EventBus<T>(
    private val config: EventBusConfig,
    scope: CoroutineScope,
    private val consumers: List<EventBusConsumer<T>>,
    private val onBufferOverflow: ((T) -> Unit)? = null,
) {
    private val logger = UnrealPluginLoggers.get<EventBus<T>>()

    private val channel =
        Channel<T>(
            capacity = config.bufferSize,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
            onUndeliveredElement = { dropped ->
                logger.warn(
                    "Bus \"${config.name}\" dropped an event due to buffer overflow, consider increasing the buffer size",
                )
                onBufferOverflow?.invoke(dropped)
            },
        )

    init {
        logger.debug("Bus \"${config.name}\" initialized with buffer size ${config.bufferSize}")
        scope.launch {
            for (event in channel) {
                consumers.forEach { consumer ->
                    consumer
                        .runCatching { consume(event) }
                        .onFailure {
                            logger.warn("An error occurred while processing an event on bus \"${config.name}\"", it)
                        }
                }
            }
        }
    }

    suspend fun dispatch(event: T) {
        logger.debug("Dispatching new event to bus \"${config.name}\"")
        channel.send(event)
    }
}
