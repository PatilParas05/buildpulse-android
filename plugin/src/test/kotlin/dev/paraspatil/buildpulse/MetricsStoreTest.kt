package dev.paraspatil.buildpulse

import dev.paraspatil.buildpulse.model.BuildMetrics
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files

class MetricsStoreTest {
    private fun tempDir(): File =Files.createTempDirectory("buildpulse-test-").toFile()

    @Test
    fun `loadPrevious returns null when file does not exist`(){
        val store=MetricsStore(File(tempDir(),"nope.json"))
        assertNull(store.loadPrevious())
    }
    @Test
    fun `save and loadPrevious round-trip correctly`(){
        val file=File(tempDir(),"build-metrics.json")
        val store=MetricsStore(file)

        val original = BuildMetrics(
            buildTimestamp = 1_700_000_000_000L,
            totalBuildTimeMs = 22_500L,
            modules = mapOf("app" to 8_000L,"lib" to 3_200L),
            tasks = mapOf(":aoo:compileDebugKotlin" to 4_100L)
        )
        store.save(original)
        assertTrue(file.exists())

        val loaded=store.loadPrevious()!!
        assertEquals(original.buildTimestamp,loaded.buildTimestamp)
        assertEquals(original.totalBuildTimeMs,loaded.totalBuildTimeMs)
        assertEquals(original.modules,loaded.modules)
        assertEquals(original.tasks,loaded.tasks)

    }
    @Test
    fun `corrupted file returns null instead of crashing`(){
        val file=File(tempDir(),"build-metrics.json")
        file.writeText("this is not json {{{")

        val store =MetricsStore(file)
        assertNull(store.loadPrevious())

    }
    @Test
    fun`save changes nested directories automatically`(){
        val deep= File(tempDir(),"a/b/c/build-metrics.json")
        val store=MetricsStore(deep)

        store.save(BuildMetrics(0L,1000L,emptyMap(),emptyMap()))
        assertTrue(deep.exists())

    }
}