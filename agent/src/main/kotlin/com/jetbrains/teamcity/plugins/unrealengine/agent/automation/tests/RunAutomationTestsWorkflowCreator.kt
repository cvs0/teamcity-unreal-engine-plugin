package com.jetbrains.teamcity.plugins.unrealengine.agent.automation.tests

import arrow.core.raise.Raise
import com.jetbrains.teamcity.plugins.framework.common.Environment
import com.jetbrains.teamcity.plugins.unrealengine.agent.UnrealBuildContext
import com.jetbrains.teamcity.plugins.unrealengine.agent.UnrealToolRegistry
import com.jetbrains.teamcity.plugins.unrealengine.agent.Workflow
import com.jetbrains.teamcity.plugins.unrealengine.agent.WorkflowCreator
import com.jetbrains.teamcity.plugins.unrealengine.agent.build.log.UnrealEngineProcessListenerFactory
import com.jetbrains.teamcity.plugins.unrealengine.agent.parseWorkflowCommand
import com.jetbrains.teamcity.plugins.unrealengine.agent.reporting.AutomationTestLogEventHandler
import com.jetbrains.teamcity.plugins.unrealengine.agent.unrealEngineCommandExecution
import com.jetbrains.teamcity.plugins.unrealengine.common.GenericError
import com.jetbrains.teamcity.plugins.unrealengine.common.UnrealPluginLoggers
import com.jetbrains.teamcity.plugins.unrealengine.common.automation.tests.RunAutomationTestsCommand

class RunAutomationTestsWorkflowCreator(
    private val toolRegistry: UnrealToolRegistry,
    private val environment: Environment,
    private val processListenerFactory: UnrealEngineProcessListenerFactory,
) : WorkflowCreator {
    companion object {
        private val logger = UnrealPluginLoggers.get<RunAutomationTestsWorkflowCreator>()
    }

    context(_: Raise<GenericError>, context: UnrealBuildContext)
    override suspend fun create(): Workflow {
        val command = parseWorkflowCommand(logger) { RunAutomationTestsCommand.from(context.runnerParameters) }
        return Workflow(
            listOf(
                unrealEngineCommandExecution(
                    environment,
                    toolRegistry.editor(context.runnerParameters).executablePath,
                    command.toArguments(),
                    processListenerFactory.create(
                        AutomationTestLogEventHandler(context),
                        reportErrorsAsBuildProblems = false,
                    ),
                ),
            ),
        )
    }
}
