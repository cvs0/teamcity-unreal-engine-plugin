package agent

import com.jetbrains.teamcity.plugins.framework.agent.AgentParametersProvider
import com.jetbrains.teamcity.plugins.framework.agent.PrimaryAgentParametersSupplier
import com.jetbrains.teamcity.plugins.framework.agent.TeamCityParameter
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class PrimaryAgentParametersSupplierTests {
    @Test
    fun `failure in one of the providers does not affect the final result`() {
        val fooParameter = TeamCityParameter("foo", "foo")
        val fooProvider = AgentParametersProvider { listOf(fooParameter) }
        val barParameter = TeamCityParameter("bar", "bar")
        val barProvider = AgentParametersProvider { listOf(barParameter) }
        val errorProneProvider = AgentParametersProvider { throw Exception("something went wrong") }
        val supplier = PrimaryAgentParametersSupplier(listOf(fooProvider, barProvider, errorProneProvider))

        val parameters = supplier.getParameters()

        assertNotNull(parameters)
        assertContains(parameters, fooParameter.key)
        assertContains(parameters, barParameter.key)
    }

    @Test
    fun `should perform parameters discovery only once`() {
        var counter = 0
        val countProvider =
            AgentParametersProvider {
                counter++
                listOf()
            }
        val supplier = PrimaryAgentParametersSupplier(listOf(countProvider))

        supplier.getParameters()
        supplier.environmentVariables
        supplier.systemProperties
        supplier.getParameters()

        assertEquals(1, counter)
    }

    @Test
    fun `should return configuration parameters`() {
        val supplier =
            PrimaryAgentParametersSupplier(
                listOf(AgentParametersProvider { listOf(TeamCityParameter("foo", "foo")) }),
            )

        val result = supplier.getParameters()

        assertEquals(1, result.size)
        assertContains(result, "foo")
    }

    @Test
    fun `should not expose environment variables or system properties`() {
        val supplier =
            PrimaryAgentParametersSupplier(
                listOf(AgentParametersProvider { listOf(TeamCityParameter("foo", "foo")) }),
            )

        assertTrue(supplier.environmentVariables.isEmpty())
        assertTrue(supplier.systemProperties.isEmpty())
    }
}
