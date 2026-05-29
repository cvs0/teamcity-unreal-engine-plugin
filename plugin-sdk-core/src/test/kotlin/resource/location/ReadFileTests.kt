package resource.location

import arrow.core.raise.either
import com.jetbrains.teamcity.plugins.framework.resource.location.FileSystem
import com.jetbrains.teamcity.plugins.framework.resource.location.readFile
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

internal class ReadFileTests {
    @Test
    fun `should raise an error when the provided path is invalid`() {
        val context = object: FileSystem {
            override fun pathOf(fileName: String): Path {
                throw InvalidPathException("", fileName)
            }
        }

        val result = with(context) { either { readFile("foo") } }

        val error = result.leftOrNull()
        assertNotNull(error)
        assertIs<InvalidPathException>(error.exception)
    }

    @Test
    fun `should raise an error when the file does not exist`(@TempDir tempDir: Path) {
        val context = tempDirFileSystem(tempDir)

        val result = with(context) { either { readFile("missing.txt") } }

        assertNotNull(result.leftOrNull())
    }

    @Test
    fun `should raise an error when the requested path is a directory`(@TempDir tempDir: Path) {
        Files.createDirectory(tempDir.resolve("dir"))
        val context = tempDirFileSystem(tempDir)

        val result = with(context) { either { readFile("dir") } }

        assertNotNull(result.leftOrNull())
    }

    @Test
    fun `should return a reader`(@TempDir tempDir: Path) {
        val content = """
            foo
            bar
        """.trimIndent()
        Files.writeString(tempDir.resolve("foo"), content)
        val context = tempDirFileSystem(tempDir)

        val result = with(context) { either { readFile("foo") } }

        val reader = result.getOrNull()
        assertNotNull(reader)
        assertEquals(reader.readText(), content)
    }

    private fun tempDirFileSystem(tempDir: Path): FileSystem =
        object : FileSystem {
            override fun pathOf(fileName: String) = tempDir.resolve(fileName)
        }
}
