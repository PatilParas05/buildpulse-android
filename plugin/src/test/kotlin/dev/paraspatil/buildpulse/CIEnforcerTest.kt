package dev.paraspatil.buildpulse

import dev.paraspatil.buildpulse.model.BuildDiff
import dev.paraspatil.buildpulse.model.BuildMetrics
import org.gradle.api.GradleException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.collections.emptySet

class CIEnforcerTest {

    private val previous = BuildMetrics(
        buildTimestamp = 1_000_000L,
        totalBuildTimeMs = 15_000L,
        modules = mapOf("app" to 5_000L, "lib" to 3_000L),
        tasks = mapOf(":app:compileDebugKotlin" to 2_100L)
    )
    private val regressing= BuildMetrics(
        buildTimestamp = 2_000_000L,
        totalBuildTimeMs = 16_200L,
        modules = mapOf("app" to 5_800L, "lib" to 3_000L),
        tasks = mapOf(":app:compileDebugKotlin" to 2_900L)
    )
    private val regressingDiff= BuildDiff(
        totalDiffMs = 1_200L,
        moduleDiffs = mapOf("app" to 800L, "lib" to 0L),
        taskDiffs = mapOf(":app:compileDebugKotlin" to 800L),
        newModules = emptySet(),
        removedModules = emptySet()
    )

    @Test
    fun `throws when failsOnRegression is true and violation exist`(){
        assertThrows (GradleException::class.java){
            CIEnforcer.evaluate(
                diff = regressingDiff,
                previous = previous,
                current = regressing,
                failOnRegression = true,
                maxAllowedIncreaseMs = 500L
            )
        }
    }
    @Test
    fun `does NOT throws when failsOnRegression is false`(){
        assertDoesNotThrow{
            CIEnforcer.evaluate(
                diff = regressingDiff,
                previous = previous,
                current = regressing,
                failOnRegression = false,
                maxAllowedIncreaseMs = 500L
            )
        }
    }
    @Test
    fun `does NOT throw when diffs are under threshold`(){
        val smallDiff= BuildDiff(
            totalDiffMs = 100L,
            moduleDiffs = mapOf("app" to 50L),
            taskDiffs = mapOf(":app:compileDebugKotlin" to 50L),
            newModules = emptySet(),
            removedModules = emptySet()
        )

        val smallCurrent=previous.copy(
            totalBuildTimeMs = 15_100L,
            modules = mapOf("app" to 5_050L,"lib" to 3_000L)
        )

        assertDoesNotThrow {
        CIEnforcer.evaluate(
            diff = smallDiff,
            previous = previous,
            current = smallCurrent,
            failOnRegression = false,
            maxAllowedIncreaseMs = 500L
        )

        }
    }
    @Test
    fun `is a no-op when diff is null (first run)`(){
        assertDoesNotThrow {
            CIEnforcer.evaluate(
                diff = null,
                previous = null,
                current = previous,
                failOnRegression = false,
                maxAllowedIncreaseMs = 500L
            )
        }
    }

}