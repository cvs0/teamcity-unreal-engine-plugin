package com.jetbrains.teamcity.plugins.framework.resource.location

import arrow.core.Either
import arrow.core.raise.either
import com.jetbrains.teamcity.plugins.framework.common.CommandLineRunner
import com.jetbrains.teamcity.plugins.framework.common.Environment
import com.jetbrains.teamcity.plugins.framework.common.OSType
import com.jetbrains.teamcity.plugins.framework.common.TeamCityLoggers
import com.jetbrains.teamcity.plugins.framework.resource.location.queries.ResourceLocationContext
import com.jetbrains.teamcity.plugins.framework.resource.location.queries.ResourceLocationQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.nio.file.Path

sealed interface ResourceLocationResult<out T> {
    data class Success<T>(
        val data: T,
    ) : ResourceLocationResult<T>

    data class Error(
        val message: String,
        val exception: Throwable? = null,
    ) : ResourceLocationResult<Nothing>
}

class ResourceLocationContextImpl : ResourceLocationContext {
    override fun pathOf(fileName: String): Path = Path.of(fileName)

    override val commandLineRunner = CommandLineRunner()
}

class QueryBuilder<T>(
    private val os: OSType,
) {
    private val queries = mutableListOf<ResourceLocationQuery<T>>()

    fun macos(vararg init: ResourceLocationQuery<*>.() -> ResourceLocationQuery<T>) {
        if (os == OSType.MacOs) {
            addQueries(init)
        }
    }

    fun linux(vararg init: ResourceLocationQuery<*>.() -> ResourceLocationQuery<T>) {
        if (os == OSType.Linux) {
            addQueries(init)
        }
    }

    fun windows(vararg init: ResourceLocationQuery<*>.() -> ResourceLocationQuery<T>) {
        if (os == OSType.Windows) {
            addQueries(init)
        }
    }

    fun anyOS(vararg init: ResourceLocationQuery<*>.() -> ResourceLocationQuery<T>) {
        addQueries(init)
    }

    private fun addQueries(init: Array<out ResourceLocationQuery<*>.() -> ResourceLocationQuery<T>>) {
        queries.addAll(
            init.map {
                it(ResourceLocationQuery { })
            },
        )
    }

    context(context: ResourceLocationContext)
    internal suspend fun executeQueries(): List<ResourceLocationResult<T>> =
        coroutineScope {
            queries
                .map {
                    async(Dispatchers.IO) {
                        when (val res = either { it.execute() }) {
                            is Either.Left -> res.value
                            is Either.Right -> ResourceLocationResult.Success(res.value)
                        }
                    }
                }.awaitAll()
        }
}

class ResourceLocator(
    private val environment: Environment,
    private val context: ResourceLocationContext,
) {
    private val logger = TeamCityLoggers.get<ResourceLocator>()

    init {
        logger.info("The operating system the location process is running on is ${environment.osType}")
    }

    suspend fun <T> locateResources(init: QueryBuilder<T>.() -> Unit): List<ResourceLocationResult<T>> {
        with(context) {
            return QueryBuilder<T>(environment.osType)
                .apply(init)
                .executeQueries()
        }
    }
}
