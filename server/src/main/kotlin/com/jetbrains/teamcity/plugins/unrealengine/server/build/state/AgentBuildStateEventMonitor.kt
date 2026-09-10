package com.jetbrains.teamcity.plugins.unrealengine.server.build.state

import arrow.core.raise.Raise
import com.jetbrains.teamcity.plugins.unrealengine.common.Error
import com.jetbrains.teamcity.plugins.unrealengine.common.build.events.AgentBuildEvent
import com.jetbrains.teamcity.plugins.unrealengine.common.ensureNotNull
import com.jetbrains.teamcity.plugins.unrealengine.server.build.agent.AgentBuildEventHandler
import jetbrains.buildServer.serverSide.BuildsManager

class AgentBuildStateEventMonitor(
    private val stateTracker: DistributedBuildStateTracker,
    private val buildManager: BuildsManager,
) : AgentBuildEventHandler {
    context(_: Raise<Error>)
    override suspend fun handleBuildEvent(
        buildId: Long,
        event: AgentBuildEvent,
    ) {
        val build =
            ensureNotNull(
                buildManager.findBuildInstanceById(buildId),
                "Unable to find a build with the given id $buildId",
            )

        stateTracker.handleBuildEvent(DistributedBuildEvent.FromAgent(build, event))
    }
}
