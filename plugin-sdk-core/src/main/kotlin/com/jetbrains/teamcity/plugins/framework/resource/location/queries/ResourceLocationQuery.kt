package com.jetbrains.teamcity.plugins.framework.resource.location.queries

import arrow.core.raise.Raise
import com.jetbrains.teamcity.plugins.framework.common.CommandLineRunner
import com.jetbrains.teamcity.plugins.framework.resource.location.AcceptFilter
import com.jetbrains.teamcity.plugins.framework.resource.location.FileSystem
import com.jetbrains.teamcity.plugins.framework.resource.location.ReadContinuationDecision
import com.jetbrains.teamcity.plugins.framework.resource.location.ResourceLocationResult
import com.jetbrains.teamcity.plugins.framework.resource.location.filteredLines
import com.jetbrains.teamcity.plugins.framework.resource.location.parseIni
import com.jetbrains.teamcity.plugins.framework.resource.location.parseJson
import com.jetbrains.teamcity.plugins.framework.resource.location.readFile
import com.jetbrains.teamcity.plugins.framework.resource.location.windows.registry.WindowsRegistrySearchFilter
import com.jetbrains.teamcity.plugins.framework.resource.location.windows.registry.windowsRegistry
import kotlinx.serialization.json.Json
import java.io.Reader

interface ResourceLocationContext : FileSystem {
    val commandLineRunner: CommandLineRunner
}

class ResourceLocationQuery<T> @PublishedApi internal constructor(
    getValue: context(ResourceLocationContext, Raise<ResourceLocationResult.Error>) () -> T,
) {
    private val getValue = getValue

    context(context: ResourceLocationContext, raise: Raise<ResourceLocationResult.Error>)
    @PublishedApi
    internal fun execute(): T = getValue(context, raise)

    /**
     * Checks existence of a given file and reads it.
     * @param fileName name of the file to read (absolute path).
     */
    fun file(fileName: String) =
        ResourceLocationQuery {
            readFile(fileName)
        }

    /**
     * Tries to read a Windows registry node and returns its entries matching the specified filter.
     */
    fun registry(
        path: String,
        filter: WindowsRegistrySearchFilter,
    ) = ResourceLocationQuery {
        windowsRegistry(path, filter)
    }
}

/**
 * Parses json content using current reader in [ResourceLocationQuery].
 */
inline fun <reified T> ResourceLocationQuery<Reader>.json(json: Json) =
    ResourceLocationQuery {
        execute().parseJson<T>(json)
    }

/**
 * Parses ini content using current reader in [ResourceLocationQuery].
 */
fun ResourceLocationQuery<Reader>.ini(sectionName: String) =
    ResourceLocationQuery {
        execute().parseIni(sectionName)
    }

/**
 * Reads lines using current reader in [ResourceLocationQuery].
 */
fun ResourceLocationQuery<Reader>.filteredLines(
    accept: AcceptFilter,
    continueReading: ReadContinuationDecision,
) = ResourceLocationQuery {
    execute().filteredLines(accept, continueReading)
}

fun <T, R> ResourceLocationQuery<T>.map(transform: (T) -> R) =
    ResourceLocationQuery {
        transform(execute())
    }
