package it.unibo.scafi.simulations

import it.unibo.alchemist.boundary.Loader

/**
 * How a simulation is run: with the Swing GUI, or headless until [Batch.maxTime].
 * This replaces the `--override` snippets that Gradle passes to the YAML simulations.
 */
sealed interface LaunchMode {
    /** Interactive run with the Swing GUI. */
    data object Gui : LaunchMode

    /** Headless run, terminating after [maxTime]. */
    data class Batch(val maxTime: Double) : LaunchMode
}

private val simulations: Map<String, (LaunchMode) -> Loader> = mapOf(
    "aggregateProcesses" to ::aggregateProcesses,
    "helloScafi" to ::helloScafi,
    "selforgCoordRegions" to ::selforgCoordRegions,
)

/**
 * Usage: `<simulationName> <batch: true|false> <maxTime>`.
 */
fun main(args: Array<String>) {
    val (name, batch, maxTime) = args
    val simulation = requireNotNull(simulations[name]) {
        "Unknown simulation '$name', available: ${simulations.keys}"
    }
    simulation(if (batch.toBoolean()) LaunchMode.Batch(maxTime.toDouble()) else LaunchMode.Gui).launch()
}
