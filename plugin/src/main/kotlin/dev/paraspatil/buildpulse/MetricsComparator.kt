package dev.paraspatil.buildpulse

import dev.paraspatil.buildpulse.model.BuildDiff
import dev.paraspatil.buildpulse.model.BuildMetrics

object MetricsComparator {
    fun compare(previous: BuildMetrics?,current: BuildMetrics): BuildDiff?{
        if (previous==null)return null

        val totalDiff=current.totalBuildTimeMs-previous.totalBuildTimeMs
        val moduleDiffs=diffMap(previous.modules,current.modules)
        val taskDiffs=diffMap(previous.tasks,current.tasks)

        val newModules=current.modules.keys-previous.modules.keys
        val removedModules=previous.modules.keys-current.modules.keys

        return BuildDiff(
            totalDiffMs = totalDiff,
            moduleDiffs = moduleDiffs,
            taskDiffs = taskDiffs,
            newModules = newModules,
            removedModules = removedModules
        )

    }
    private fun diffMap(
        previous: Map<String, Long>,
        current: Map<String, Long>
    ): Map<String, Long> {
        val shared=previous.keys.intersect(current.keys)
        return shared.associate { key ->
            key to (current[key] ?: 0) - (previous[key] ?: 0)
        }
    }
}