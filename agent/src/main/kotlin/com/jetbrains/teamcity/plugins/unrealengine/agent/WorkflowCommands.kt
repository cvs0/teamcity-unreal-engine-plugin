package com.jetbrains.teamcity.plugins.unrealengine.agent

import arrow.core.NonEmptyList
import arrow.core.getOrElse
import arrow.core.raise.Raise
import arrow.core.raise.either
import com.intellij.openapi.diagnostic.Logger
import com.jetbrains.teamcity.plugins.framework.common.Environment
import com.jetbrains.teamcity.plugins.unrealengine.common.GenericError
import com.jetbrains.teamcity.plugins.unrealengine.common.PropertyValidationError
import com.jetbrains.teamcity.plugins.unrealengine.common.UnrealCommand
import com.jetbrains.teamcity.plugins.unrealengine.common.raise
import jetbrains.buildServer.agent.runner.ProcessListener

context(_: Raise<GenericError>, context: UnrealBuildContext)
fun <T : UnrealCommand> parseWorkflowCommand(
    logger: Logger,
    parse: context(Raise<NonEmptyList<PropertyValidationError>>) () -> T,
): T =
    either { parse() }.getOrElse {
        it.forEach { error ->
            logger.error("An error occurred during command creation: ${error.message}")
        }
        raise("Unable to create command from the given runner parameters")
    }

context(context: UnrealBuildContext)
fun unrealEngineCommandExecution(
    environment: Environment,
    executablePath: String,
    arguments: List<String>,
    processListener: ProcessListener,
) = UnrealEngineCommandExecution(
    UnrealEngineProgramCommandLine(
        environment,
        context.buildParameters.environmentVariables,
        context.workingDirectory,
        executablePath,
        arguments,
    ),
    processListener,
)
