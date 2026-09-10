package com.jetbrains.teamcity.plugins.framework.resource.location

import arrow.core.raise.Raise
import arrow.core.raise.context.ensure
import arrow.core.raise.context.raise
import java.io.Reader

data class IniProperty(
    val key: String,
    val value: String,
)

context(_: Raise<ResourceLocationResult.Error>)
internal fun Reader.parseIni(sectionName: String): List<IniProperty> {
    val properties = mutableListOf<IniProperty>()
    var currentSection: String? = null
    var foundSection = false

    try {
        for (rawLine in readLines()) {
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith(';') || line.startsWith('#')) {
                continue
            }

            if (line.startsWith('[') && line.endsWith(']')) {
                currentSection = line.substring(1, line.length - 1)
                if (currentSection == sectionName) {
                    foundSection = true
                }
                continue
            }

            if (currentSection != sectionName) {
                continue
            }

            val separator = line.indexOf('=')
            ensure(separator > 0) {
                raise(ResourceLocationResult.Error("Malformed ini entry: $line"))
            }

            properties +=
                IniProperty(
                    key = line.substring(0, separator).trim(),
                    value = line.substring(separator + 1).trim(),
                )
        }
    } catch (e: Throwable) {
        raise(ResourceLocationResult.Error("Unknown error occurred during ini config read", e))
    } finally {
        close()
    }

    ensure(foundSection) {
        raise(ResourceLocationResult.Error("Specified section $sectionName does not exist"))
    }

    return properties
}
