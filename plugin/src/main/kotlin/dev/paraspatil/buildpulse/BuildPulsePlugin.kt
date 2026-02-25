package dev.paraspatil.buildpulse

import org.gradle.api.Project
import org.gradle.api.Plugin

import java.io.File

class BuildPulsePlugin : Plugin<Project> {
    @Suppress("DEPRECATION")
    override fun apply(project: Project) {
        if (project != project.rootProject){
            project.logger.warn("[BuildPulse] This plugin should only be applied to the root project")
            return
        }

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

            if (ext.generatedHtmlReport){
                val htmlFile = File(outputDir, "build-metrics.html")
                HtmlReportGenerator.generate(current, diff, htmlFile, ext.maxAllowedIncreaseMs)
                project.logger.lifecycle("[BuildPulse] HTML report  ${htmlFile.absolutePath}")

            }
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