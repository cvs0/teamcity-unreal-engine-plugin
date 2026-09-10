package com.jetbrains.teamcity.plugins.unrealengine.common

import com.intellij.openapi.diagnostic.Logger

class UnrealPluginLoggers {
    companion object {
        // teamcity-agent.log / teamcity-server.log
        inline fun <reified T> get() = Logger.getInstance("jetbrains.buildServer.unrealEngine." + T::class.java.name)
    }
}
