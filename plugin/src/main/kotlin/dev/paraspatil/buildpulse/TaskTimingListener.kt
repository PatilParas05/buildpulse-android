package dev.paraspatil.buildpulse

import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskState
import java.util.concurrent.ConcurrentHashMap

class TaskTimingListener(private val logger: Logger) : TaskExecutionListener {

    private val startTimes = ConcurrentHashMap<String, Long>()
    private val duration = ConcurrentHashMap<String, Long>()

    val tskDuration: Map<String, Long>
        get() = duration.toMap()

    override fun beforeExecute(task: Task) {
        startTimes[task.path] = System.nanoTime()
        logger.debug("[BuildPulse] Task started: ${task.path}")
    }

    override fun afterExecute(task: Task, state: TaskState) {
        val start = startTimes.remove(task.path) ?: return
        val elapsedMs = (System.nanoTime() - start) / 1_000_000
        duration[task.path] = elapsedMs
        logger.debug("[BuildPulse] Task finished: ${task.path} in $elapsedMs ms")
    }
}