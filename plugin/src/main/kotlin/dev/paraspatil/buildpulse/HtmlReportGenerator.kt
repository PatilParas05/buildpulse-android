package dev.paraspatil.buildpulse

import dev.paraspatil.buildpulse.model.BuildDiff
import dev.paraspatil.buildpulse.model.BuildMetrics
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

object HtmlReportGenerator {

    fun generate(
        current: BuildMetrics,
        diff: BuildDiff?,
        outputFilePath: File,
        maxAllowedIncreaseMs: Long
    ) {
        val html = buildHtmlReport(current, diff, maxAllowedIncreaseMs)
        outputFilePath.parentFile?.mkdirs()
        outputFilePath.writeText(html)

    }

    private fun buildHtmlReport(
        current: BuildMetrics,
        diff: BuildDiff?,
        threshold: Long
    ): String {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(current.buildTimestamp))
        val totalSec = current.totalBuildTimeMs / 1_000.0
        return """
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>BuildPulse Report - ${timestamp}</title>
       <style>
          * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            padding: 40px 20px;
            min-height: 100vh;
        }
        
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background: white;
            border-radius: 16px;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
            overflow: hidden;
        }
        
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 40px;
            text-align: center;
        }
        
        .header h1 {
            font-size: 48px;
            margin-bottom: 10px;
            font-weight: 700;
        }
        
        .header .subtitle {
            font-size: 18px;
            opacity: 0.9;
        }
        
        .summary {
            padding: 40px;
            background: #f8f9fa;
            border-bottom: 1px solid #e9ecef;
        }
        
        .summary-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
            margin-top: 20px;
        }
        
        .summary-card {
            background: white;
            padding: 24px;
            border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
            border-left: 4px solid #667eea;
        }
        
        .summary-card.regression {
            border-left-color: #f56565;
        }
        
        .summary-card.improvement {
            border-left-color: #48bb78;
        }
        
        .summary-card .label {
            font-size: 14px;
            color: #718096;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 8px;
        }
        
        .summary-card .value {
            font-size: 32px;
            font-weight: 700;
            color: #2d3748;
        }
        
        .summary-card .diff {
            font-size: 16px;
            margin-top: 8px;
            font-weight: 600;
        }
        
        .diff.positive {
            color: #f56565;
        }
        
        .diff.negative {
            color: #48bb78;
        }
        
        .modules {
            padding: 40px;
        }
        
        .section-title {
            font-size: 28px;
            font-weight: 700;
            margin-bottom: 24px;
            color: #2d3748;
        }
        
        .module-list {
            display: flex;
            flex-direction: column;
            gap: 16px;
        }
        
        .module-item {
            background: white;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
            padding: 20px;
            transition: all 0.2s;
        }
        
        .module-item:hover {
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            transform: translateY(-2px);
        }
        
        .module-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .module-name {
            font-size: 20px;
            font-weight: 600;
            color: #2d3748;
        }
        
        .module-time {
            font-size: 24px;
            font-weight: 700;
            color: #667eea;
        }
        
        .module-diff {
            margin-top: 8px;
            font-size: 14px;
            font-weight: 600;
        }
        
        .badge {
            display: inline-block;
            padding: 4px 12px;
            border-radius: 12px;
            font-size: 12px;
            font-weight: 600;
            margin-left: 12px;
        }
        
        .badge.new {
            background: #bee3f8;
            color: #2c5282;
        }
        
        .badge.regression {
            background: #fed7d7;
            color: #c53030;
        }
        
        .badge.improvement {
            background: #c6f6d5;
            color: #22543d;
        }
        
        .footer {
            padding: 24px 40px;
            background: #f8f9fa;
            text-align: center;
            color: #718096;
            font-size: 14px;
        }
        
        .progress-bar {
            width: 100%;
            height: 8px;
            background: #e2e8f0;
            border-radius: 4px;
            margin-top: 12px;
            overflow: hidden;
        }
        
        .progress-fill {
            height: 100%;
            background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
            border-radius: 4px;
            transition: width 0.3s;
        }
       </style>
    </head>
    <body>
        <div class="container">
            <div class="header">
                <h1> BuildPulse</h1>
            <div class="subtitle">Build Performance Report • ${timestamp} </div>
            </div>
            <div class="summary">
                <h2 class="section-title">Build Summary</h2>
                <div class="summary-grid">
                    <div class="summary-card ${getCardClass(diff?.totalDiffMs, threshold)}">
                        <div class="label">Total Build Time</div>
                        <div class="value">${"%.1f".format(totalSec)}s</div>
                            ${renderDiff(diff?.totalDiffMs, threshold)}
                    </div>
                    <div class="summary-card">
                        <div class="label">Modules Built</div>
                        <div class="value">${current.modules.size}</div>
                    </div>
                    <div class="summary-card">
                        <div class="label">Tasks Executed</div>
                        <div class="value">${current.tasks.size}</div>
                    </div>
                    ${renderRegressionCard(diff, threshold)}
                </div>
            </div>
            <div class="modules">
                <h2 class="section-title">Module Breakdown</h2>
                <div class="module-list">
                    ${renderModules(current, diff, threshold)}
                </div>
            </div>
            <div  class="footer">
                Generated by BuildPulse • Threshold: ${threshold}ms 
            </div>
        </div>
    </body>
    </html>
    
""".trimIndent()
    }

    private fun getCardClass(diffMs: Long?, threshold: Long): String {
        if (diffMs == null) return ""
        return when {
            diffMs > threshold -> "regression"
            diffMs < 0 -> "improvement"
            else -> ""
        }
    }

    private fun renderDiff(diffMs: Long?, threshold: Long): String {
        if (diffMs == null) return "<div class = 'diff'>First run </div>"

        val className = when {
            diffMs > threshold -> "regression"
            diffMs > 0 -> "negative"
            diffMs < 0 -> "positive"
            else -> ""
        }
        val icon = when {
            diffMs > threshold -> "⚠️"
            diffMs > 0 -> "↗"
            diffMs < 0 -> "✅"
            else -> "="
        }
        val sign = if (diffMs > 0) "+" else ""
        return "<div class ='diff $className'>$icon ${sign}${diffMs}ms</div>"

    }

    private fun renderRegressionCard(diff: BuildDiff?, threshold: Long): String {
        if (diff == null) return ""

        val regressions = diff.moduleDiffs.count { it.value > threshold }
        val cardClass = if (regressions > 0) "regression" else "improvement"

        return """
            <div class="summary-card $cardClass">
                <div class="label">Regressions</div>
                <div class="value">$regressions</div>
                </div>
        """.trimIndent()
    }

    private fun renderModules(
        current: BuildMetrics,
        diff: BuildDiff?,
        threshold: Long
    ): String {
        val sorted = current.modules.entries.sortedByDescending { it.value }
        val maxTime = sorted.firstOrNull()?.value ?: 1L

        return sorted.joinToString("\n") { (module, timeMs) ->
            val diffMs = diff?.moduleDiffs?.get(module)
            val isNew = diff?.newModules?.contains(module) == true
            val isRegression = diffMs != null && diffMs > threshold

            val badge = when {
                isNew -> "<span class='badge new'>NEW</span>"
                isRegression -> "<span class='badge regression'>REGRESSION</span>"
                diffMs != null && diffMs < 0 -> "<span class='badge improvement'>IMPROVED</span>"
                else -> ""
            }

            val diffHtml = if (diffMs != null) {
                val sign = if (diffMs > 0) "+" else ""
                val className = if (diffMs > 0) "positive" else "negative"
                "<div class='module-diff diff $className'>$sign${diffMs}ms from previous build</div>"
            } else {
                ""
            }

            val percentage = if (maxTime > 0) {
                (timeMs.toDouble() / maxTime * 100).toInt()
            } else {
                0
            }

            """
            <div class="module-item">
                <div class="module-header">
                    <div>
                        <span class="module-name">$module</span>
                        $badge
                    </div>
                    <div class="module-time">${"%,d".format(timeMs)} ms</div>
                </div>
                $diffHtml
                <div class="progress-bar">
                    <div class="progress-fill" style="width: ${percentage}%"></div>
                </div>
            </div>
        """.trimIndent()
        }
    }
}