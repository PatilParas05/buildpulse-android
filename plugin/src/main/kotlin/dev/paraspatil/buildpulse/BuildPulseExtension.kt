package dev.paraspatil.buildpulse

open class BuildPulseExtension{
    var enabled : Boolean=true
    var trackTasks: Boolean=true
    var trackModules: Boolean=true
    var failOnRegression: Boolean=false
    var maxAllowedIncreaseMs: Long=500L
    var outputDir : String="buildpulse"
}