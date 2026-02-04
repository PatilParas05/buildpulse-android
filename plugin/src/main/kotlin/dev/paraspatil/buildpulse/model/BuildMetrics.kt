package dev.paraspatil.buildpulse.model


data class BuildMetrics(
    val buildTimestamp : Long,
    val totalBuildTimeMs: Long,
    val modules:Map<String, Long>,
    val tasks:Map<String, Long>,
)

data class BuildDiff(
    val totalDiffMs: Long,
    val moduleDiffs: Map<String, Long>,
    val taskDiffs: Map<String, Long>,
    val newModules: Set<String>,
    val removedModules: Set<String>
)

data class RegressionViolation(
    val name : String,
    val type: ViolationType,
    val previousMs: Long,
    val currentMs: Long,
    val diffMs: Long,
)

enum class ViolationType{MODULE,TASK,TOTAL}

