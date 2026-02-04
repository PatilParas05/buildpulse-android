package dev.paraspatil.buildpulse

import dev.paraspatil.buildpulse.model.BuildMetrics

class MetricsCollector(
    private val listener: TaskTimingListener,
    private val buildStartTimeMs: Long
){
    fun collect(buildEndTimeMs: Long): BuildMetrics {

        val taskDuration=listener.tskDuration
        val moduleDuration=rollUpToModules(taskDuration)

        return BuildMetrics(
            buildTimestamp = buildStartTimeMs,
            totalBuildTimeMs = buildEndTimeMs - buildStartTimeMs,
            modules = moduleDuration,
            tasks = taskDuration
        )
    }

    private fun rollUpToModules(task:Map<String, Long>): Map<String, Long> {
        val result = mutableMapOf<String, Long>()
        for ((path, duration) in task) {
            val module = extractModule(path)
            result[module] = (result[module] ?: 0) + duration
        }
        return result
    }

    private fun extractModule(taskPath: String): String {
        val parts = taskPath.trimStart(':').split(':')
        return if (parts.size>=2)parts [0] else  "root"

    }
}