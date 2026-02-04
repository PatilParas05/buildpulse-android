package dev.paraspatil.buildpulse

import dev.paraspatil.buildpulse.model.BuildMetrics
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MetricsComparatorTest {

    private val baseline= BuildMetrics(
        buildTimestamp = 1_000_000L,
        totalBuildTimeMs = 15_000L,
        modules = mapOf(
            "app" to 5_000L,
            "feature-login" to 3_000L,
            "core-network" to 2_800L
        ),
        tasks = mapOf(
            ":app:compileDebugKotlin" to 2_100L,
            ":feature-login:compileDebugKotlin"  to 1_200L
        )
    )
    @Test
    fun `returns null when there is no previous build`(){
        val current=baseline.copy(buildTimestamp = 2_000_000L)
        assertNull(MetricsComparator.compare(null,current))

    }
    @Test
    fun `detects positive module regressions`(){
        val current = baseline.copy(
            totalBuildTimeMs = 16_200L,
            modules = baseline.modules+("feature-login" to 3_420L)
        )
        val diff=MetricsComparator.compare(baseline,current)!!

        assertEquals(1_200L,diff.totalDiffMs)
        assertEquals(420L,diff.moduleDiffs["feature-login"])

    }

    @Test
    fun `detects negative module improvement`(){
        val current = baseline.copy(
            totalBuildTimeMs = 14_880L,
            modules = baseline.modules+("core-network" to 2_680L)
        )
        val diff=MetricsComparator.compare(baseline,current)!!
        assertEquals(-120L,diff.moduleDiffs["core-network"])

    }
}
