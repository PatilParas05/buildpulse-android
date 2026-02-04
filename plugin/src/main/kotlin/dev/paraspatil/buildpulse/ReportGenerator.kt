package dev.paraspatil.buildpulse

import dev.paraspatil.buildpulse.model.BuildDiff
import dev.paraspatil.buildpulse.model.BuildMetrics

class ReportGenerator {
    fun printReport(
        current: BuildMetrics,
        diff: BuildDiff,
        maxAllowedIncreaseMs: Long
    ){
        val lines=mutableListOf<String>()

        lines+=""
        lines += "╔═══════════════════════════════════════════════════╗"
        lines += "║          📊  BuildPulse — Metrics Summary         ║"
        lines += "╚═══════════════════════════════════════════════════╝"
        lines += ""

        val totalSec=current.totalBuildTimeMs/1_000.0
        val totalLabel=if (diff!=null)formatDiff(diff.totalDiffMs,maxAllowedIncreaseMs) else "(first run)"
        lines+="Total build time: ${"%.1f".format(totalSec)}seconds ($totalLabel)"
        lines+=""

        lines+="Module Breakdown (sorted by time ↓)"
        lines += "  ─────────────────────────────────────"

        val sorted=current.modules.entries.sortedByDescending { it.value }
        for ((module,timeMs)in sorted){
            val diffLabel=when{
                diff !=null && module in diff.moduleDiffs ->
                    formatDiff(diff.moduleDiffs[module]!!,maxAllowedIncreaseMs)
                diff != null && module in diff.newModules -> "(new)"
                else -> ""
            }
            lines+="     %-22s %s   %s".format(module,formatMs(timeMs),diffLabel)
        }

        if (diff !=null && diff.removedModules.isNotEmpty()){
            lines+=""
            lines+="Removed modules:"
            for (m in diff.removedModules){
                lines+="   $m    (no longer build)"
            }
        }

        lines+=""
        lines += "═══════════════════════════════════════════════════════"
        lines += ""

        lines.forEach { println(it) }
        }

    private fun formatDiff(diffMs: Long, threshold: Long): String = when{
        diffMs>threshold -> "(+${diffMs} ms ⚠️  regression)"
        diffMs>0 -> "(+${diffMs} ms)"
        diffMs<0 -> "(-${diffMs} ms ✅)"
        else -> "(no change)"

    }
    private fun formatMs(ms: Long): String ="%,d ms".format(ms)
}