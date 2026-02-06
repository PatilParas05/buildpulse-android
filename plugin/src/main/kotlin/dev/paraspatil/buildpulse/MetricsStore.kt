package dev.paraspatil.buildpulse

import dev.paraspatil.buildpulse.model.BuildMetrics
import jdk.internal.platform.Container.metrics

import java.io.File

class MetricsStore(private val metricsFile: File){

    fun save(metrics: BuildMetrics){
       val parentDir =metricsFile.parentFile
        if (parentDir!=null && !parentDir.exists()){
            val created=parentDir.mkdirs()
            if (!created && !parentDir.exists()) {
                throw IllegalStateException(
                    "Failed to create parent directories :${parentDir.absolutePath}"
                )
            }
        }

    try{
        metricsFile.writeText(serialize(metrics))
    }catch (e: Exception)
    {
        throw IllegalStateException(
            "Failed to save metrics to ${metricsFile.absolutePath}", e
        )
    }
}

    fun loadPrevious(): BuildMetrics?{
        if (!metricsFile.exists())return null
        return try {
            deserialize(metricsFile.readText())
        }catch (e: Exception){
            null
        }
    }

    private fun serialize(m: BuildMetrics): String{
       return buildString {
           appendLine("{")
           appendLine("    \"buildTimestamp\": ${m.buildTimestamp},")
           appendLine("    \"totalBuildTimeMs\": ${m.totalBuildTimeMs},")
           append("  \"modules\": ")
           append(mapToJson(m.modules))
           appendLine(",")
           append("    \"tasks\": ")
           append(mapToJson(m.tasks))
           appendLine()
            append("}")
       }
    }

    private fun deserialize(json: String): BuildMetrics{
        val timestamp=extractLong( json,"buildTimestamp")
        val totalTime=extractLong( json,"totalBuildTimeMs")
        val modules=extractMap( json,"modules")
        val tasks=extractMap( json,"tasks")

        return BuildMetrics(
            buildTimestamp = timestamp,
            totalBuildTimeMs = totalTime,
            modules = modules,
            tasks = tasks
        )
    }

    private fun mapToJson(map:Map<String, Long>): String{
        if (map.isEmpty())return "{}"
        val entries=map.entries.joinToString(",\n"){ (k,v) ->
            "    \"$k\": $v"
        }
        return "{\n$entries\n}"
    }

    private fun extractLong(json: String,key: String): Long{
        val regex= Regex("\"$key\"\\s*:\\s*(\\d+)")
        return regex.find(json)?.groupValues?.get(1)?.toLong()?:0L
    }

    private fun extractMap(json: String,key: String): Map<String, Long> {
        val keyIndex =json.indexOf("\"$key\"")
        if (keyIndex==-1)return emptyMap()

        val braceStart=json.indexOf('{',keyIndex+key.length)
        if (braceStart==-1)return emptyMap()

        val braceEnd=json.indexOf('}',braceStart)
        if (braceEnd==-1)return emptyMap()

        val block=json.substring(braceStart+1,braceEnd)
        val entryRegex=Regex("\"([^\"]+)\"\\s*:\\s*(\\d+)")

        return entryRegex.findAll(block).associate { match ->
        match.groupValues[1] to match.groupValues[2].toLong()
        }
    }
}