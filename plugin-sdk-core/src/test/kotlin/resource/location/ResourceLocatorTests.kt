package resource.location

import com.jetbrains.teamcity.plugins.framework.common.CommandLineRunner
import com.jetbrains.teamcity.plugins.framework.common.Environment
import com.jetbrains.teamcity.plugins.framework.common.OSType
import com.jetbrains.teamcity.plugins.framework.resource.location.QueryBuilder
import com.jetbrains.teamcity.plugins.framework.resource.location.ResourceLocationResult
import com.jetbrains.teamcity.plugins.framework.resource.location.ResourceLocator
import com.jetbrains.teamcity.plugins.framework.resource.location.queries.ResourceLocationContext
import com.jetbrains.teamcity.plugins.framework.resource.location.queries.map
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class ResourceLocatorTests {
    @Test
    fun `should only return results for MacOS when requested`(@TempDir tempDir: Path) = runTest {
        val locator = createLocator(createEnvironment(OSType.MacOs), tempDir)

        val result = locator.locateResources { buildQuery() }

        assertEquals(1, result.size)
        val locationResult = result[0]
        assertIs<ResourceLocationResult.Success<OSType>>(locationResult)
        assertEquals(OSType.MacOs, locationResult.data)
    }

    @Test
    fun `should only return results for Windows when requested`(@TempDir tempDir: Path) = runTest {
        val locator = createLocator(createEnvironment(OSType.Windows), tempDir)

        val result = locator.locateResources { buildQuery() }

        assertEquals(1, result.size)
        val locationResult = result[0]
        assertIs<ResourceLocationResult.Success<OSType>>(locationResult)
        assertEquals(OSType.Windows, locationResult.data)
    }

    @Test
    fun `should only return results for Linux when requested`(@TempDir tempDir: Path) = runTest {
        val locator = createLocator(createEnvironment(OSType.Linux), tempDir)

        val result = locator.locateResources { buildQuery() }

        assertEquals(1, result.size)
        val locationResult = result[0]
        assertIs<ResourceLocationResult.Success<OSType>>(locationResult)
        assertEquals(OSType.Linux, locationResult.data)
    }

    private fun createEnvironment(os: OSType): Environment = object : Environment {
        override val osType = os
        override val homeDirectory = Path.of("")
        override val programDataDirectory = Path.of("")
    }

    private fun createLocator(
        environment: Environment,
        tempDir: Path,
    ): ResourceLocator {
        val filePath = tempDir.resolve("foo")
        Files.writeString(filePath, environment.osType.toString())

        return ResourceLocator(
            environment,
            mockk<ResourceLocationContext> {
                every { pathOf(any()) } returns filePath
                every { commandLineRunner } returns CommandLineRunner()
            },
        )
    }

    private fun QueryBuilder<OSType>.buildQuery() {
        windows({ file("foo").map { OSType.valueOf(it.readText()) } })
        linux({ file("foo").map { OSType.valueOf(it.readText()) } })
        macos({ file("foo").map { OSType.valueOf(it.readText()) } })
    }
}
