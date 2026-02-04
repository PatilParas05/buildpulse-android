package dev.paraspatil.buildpulse

import dev.paraspatil.buildpulse.model.BuildDiff
import dev.paraspatil.buildpulse.model.BuildMetrics
import dev.paraspatil.buildpulse.model.RegressionViolation
import dev.paraspatil.buildpulse.model.ViolationType
import org.gradle.api.GradleException
import java.lang.StringBuilder

object CIEnforcer {
    fun evaluate(
        diff: BuildDiff?,
        previous: BuildMetrics?,
        current: BuildMetrics,
        failOnRegression: Boolean,
        maxAllowedIncreaseMs: Long
    ) {

        if (diff == null || previous == null || !failOnRegression) return

        val violations = collectViolations(diff, previous, current, maxAllowedIncreaseMs)
        if (violations.isEmpty()) {
            print("\n ✅  BuildPulse CI Check: no regressions detected.\n")
            return
        }

        val report = buildViolationReport(violations)
        throw GradleException(
            "\n\n  BuildPulse CI check FAILED - regression detected!\n" +
                    report +
                    "\nSet failOnRegression=false in buildPulse { } to allow regressions."
        )
    }

    private fun collectViolations(
        diff: BuildDiff,
        previous: BuildMetrics,
        current: BuildMetrics,
        threshold: Long
    ): List<RegressionViolation> {
        val violations = mutableListOf<RegressionViolation>()

        if (diff.totalDiffMs > threshold) {
            violations += RegressionViolation(
                name = "TOTAL BUILD",
                type = ViolationType.TOTAL,
                previousMs = previous.totalBuildTimeMs,
                currentMs = current.totalBuildTimeMs,
                diffMs = diff.totalDiffMs
            )
        }

        for ((module, moduleDiff) in diff.moduleDiffs) {
            if (moduleDiff > threshold) {
                violations += RegressionViolation(
                    name = module,
                    type = ViolationType.MODULE,
                    previousMs = previous.modules[module] ?: 0L,
                    currentMs = current.modules[module] ?: 0L,
                    diffMs = moduleDiff
                )
            }
        }

        for ((task, taskDiff) in diff.taskDiffs) {
            if (taskDiff > threshold) {
                violations += RegressionViolation(
                    name = task,
                    type = ViolationType.TASK,
                    previousMs = previous.tasks[task] ?: 0L,
                    currentMs = current.tasks[task] ?: 0L,
                    diffMs = taskDiff
                )
            }

        }
        return violations
    }

    private fun buildViolationReport(violations: List<RegressionViolation>): String {

        val sb = StringBuilder()
        sb.appendLine("  ┌─────────────────────────────────────────────────────┐")
        sb.appendLine("  │  Regressions  (threshold exceeded)                  │")
        sb.appendLine("  ├─────────────────────────────────────────────────────┤")

        for (v in violations) {
            val label = when (v.type) {
                ViolationType.MODULE -> "Module"
                ViolationType.TASK -> "Task"
                ViolationType.TOTAL -> "Total"
            }
            sb.appendLine("  │  [$label]  %-36s  +%,d ms".format(v.name, v.diffMs))
            sb.appendLine("  │           prev: %,d ms  →  now: %,d ms".format(v.previousMs, v.currentMs))
        }
        sb.appendLine("  └─────────────────────────────────────────────────────┘")
        return sb.toString()
    }

}
