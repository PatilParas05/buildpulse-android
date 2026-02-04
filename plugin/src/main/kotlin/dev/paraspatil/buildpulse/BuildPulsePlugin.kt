package dev.paraspatil.buildpulse

import org.gradle.api.Project
import org.gradle.api.Plugin

import java.io.File

class BuildPulsePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = project.extensions.create("buildPulse", BuildPulseExtension::class.java)

        val buildStartMs = System.currentTimeMillis()

        val listener = TaskTimingListener(project.logger)
        project.gradle.taskGraph.addTaskExecutionListener(listener)

        project.gradle.buildFinished {
            if (!ext.enabled) return@buildFinished

            val outputDir = File(project.rootDir, ext.outputDir)
            val metricsFile = File(outputDir, "build-metrics.json")

            val collector = MetricsCollector(listener, buildStartMs)
            val current = collector.collect(System.currentTimeMillis())

            val store = MetricsStore(metricsFile)
            val previous = store.loadPrevious()

            val diff = MetricsComparator.compare(previous, current)

            ReportGenerator.printReport(current, diff, ext.maxAllowedIncreaseMs)

            CIEnforcer.evaluate(
                diff = diff,
                previous = previous,
                current = current,
                failOnRegression = ext.failOnRegression,
                maxAllowedIncreaseMs = ext.maxAllowedIncreaseMs
            )

            store.save(current)
            project.logger.info("[BuildPulse] Metrics saved to ${metricsFile.absolutePath}")
        }
    }
}