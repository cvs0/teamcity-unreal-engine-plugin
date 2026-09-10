package com.jetbrains.teamcity.plugins.unrealengine.common

interface CommandExecutionContext {
    val workingDirectory: String

    fun resolvePath(
        root: String,
        vararg parts: String,
    ): String

    fun fileExists(path: String): Boolean

    fun isAbsolute(path: String): Boolean

    fun createDirectory(
        root: String,
        vararg parts: String,
    ): String

    fun resolveUserPath(path: String): String
}
