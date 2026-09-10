package com.jetbrains.teamcity.plugins.framework.agent

data class TeamCityParameter(
    val key: String,
    val value: String,
)

fun interface AgentParametersProvider {
    suspend fun provide(): List<TeamCityParameter>
}
