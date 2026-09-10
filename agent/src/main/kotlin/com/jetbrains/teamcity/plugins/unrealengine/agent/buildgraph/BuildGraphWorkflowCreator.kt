package com.jetbrains.teamcity.plugins.unrealengine.agent.buildgraph

import arrow.core.raise.Raise
import com.jetbrains.teamcity.plugins.unrealengine.agent.UnrealBuildContext
import com.jetbrains.teamcity.plugins.unrealengine.agent.UnrealEngineCommandExecution
import com.jetbrains.teamcity.plugins.unrealengine.agent.Workflow
import com.jetbrains.teamcity.plugins.unrealengine.agent.WorkflowCreator
import com.jetbrains.teamcity.plugins.unrealengine.agent.parseWorkflowCommand
import com.jetbrains.teamcity.plugins.unrealengine.common.GenericError
import com.jetbrains.teamcity.plugins.unrealengine.common.UnrealPluginLoggers
import com.jetbrains.teamcity.plugins.unrealengine.common.buildgraph.BuildGraphCommand
import com.jetbrains.teamcity.plugins.unrealengine.common.buildgraph.BuildGraphMode

class BuildGraphWorkflowCreator(
    private val singleMachineExecutor: SingleMachineExecutor,
    private val distributedExecutor: DistributedExecutor,
) : WorkflowCreator {
    companion object {
        private val logger = UnrealPluginLoggers.get<BuildGraphWorkflowCreator>()
    }

    context(_: Raise<GenericError>, context: UnrealBuildContext)
    override suspend fun create(): Workflow {
        val command = parseWorkflowCommand(logger) { BuildGraphCommand.from(context.runnerParameters) }
        val commands: List<UnrealEngineCommandExecution> =
            when (command.mode) {
                is BuildGraphMode.SingleMachine -> singleMachineExecutor.execute(command)
                is BuildGraphMode.Distributed -> distributedExecutor.execute(command)
            }
        return Workflow(commands)
    }
}
