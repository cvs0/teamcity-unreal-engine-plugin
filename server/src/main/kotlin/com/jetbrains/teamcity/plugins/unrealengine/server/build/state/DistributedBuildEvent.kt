package com.jetbrains.teamcity.plugins.unrealengine.server.build.state

import com.jetbrains.teamcity.plugins.unrealengine.common.build.events.AgentBuildEvent
import jetbrains.buildServer.serverSide.SBuild

sealed interface DistributedBuildEvent {
    val build: SBuild

    data class FromAgent(
        override val build: SBuild,
        val agentEvent: AgentBuildEvent,
    ) : DistributedBuildEvent

    data class BuildSkipped(
        override val build: SBuild,
    ) : DistributedBuildEvent
}
