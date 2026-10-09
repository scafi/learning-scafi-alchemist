package it.unibo.scafi.simulations

import it.unibo.alchemist.boundary.exporters.CSVExporter
import it.unibo.alchemist.boundary.exportfilters.CommonFilters
import it.unibo.alchemist.boundary.extractors.NumberOfNodes
import it.unibo.alchemist.boundary.extractors.Time
import it.unibo.alchemist.boundary.extractors.moleculeReader
import it.unibo.alchemist.boundary.kotlindsl.simulationOnMap
import it.unibo.alchemist.boundary.swingui.monitor.impl.SwingGUI
import it.unibo.alchemist.boundary.variables.LinearVariable
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.incarnations.ScafiIncarnation
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.maps.actions.ReproduceGPSTrace
import it.unibo.alchemist.model.maps.deployments.FromGPSTrace
import it.unibo.alchemist.model.maps.environments.oSMEnvironment
import it.unibo.alchemist.model.reactions.event
import it.unibo.alchemist.model.scafi.actions.RunScafiProgram
import it.unibo.alchemist.model.terminators.AfterTime
import it.unibo.alchemist.model.timedistributions.exponentialTime
import it.unibo.alchemist.model.times.DoubleTime
import org.apache.commons.math3.random.RandomGenerator

/**
 * Kotlin DSL equivalent of `src/main/yaml/helloScafi.yml`.
 */
fun helloScafi(mode: LaunchMode) = simulationOnMap(ScafiIncarnation<Any, GeoPosition>()) {
    val random by variable(LinearVariable(default = 2.0, min = 0.0, max = 5.0, step = 1.0))
    // `range` and `moveFrequency` are constant formulas in the YAML: plain values here
    val range = 350.0
    val moveFrequency = 1.0
    val source = 100
    val retentionTime = 5.0
    val gpsTraceFile = "vcmuser.gpx"
    val programRate = 1.0
    val exportInterval = 1.0
    val totalNodes = 1497
    val timeToAlign = 1365922800
    scenarioSeed(random.toLong())
    simulationSeed(random.toLong())
    val environment = oSMEnvironment<Any>("vcm.pbf", false)
    environment(environment) {
        networkModel(ConnectWithinDistance(range))
        deployments {
            deploy(FromGPSTrace(totalNodes, gpsTraceFile, true, "AlignToTime", timeToAlign, false, false)) {
                val node = contextOf<Node<Any>>()
                // _reactions.program
                withTimeDistribution(exponentialTime<Any>(programRate)) {
                    program(event()) {
                        action(
                            RunScafiProgram<Any, GeoPosition>(
                                environment,
                                node,
                                contextOf<Reaction<Any>>(),
                                contextOf<RandomGenerator>(),
                                "it.unibo.scafi.examples.HelloScafi",
                                retentionTime,
                            ),
                        )
                    }
                }
                program("send")
                // _reactions.move
                withTimeDistribution(moveFrequency) {
                    program(event()) {
                        action(
                            ReproduceGPSTrace(
                                environment,
                                node,
                                contextOf<Reaction<Any>>(),
                                gpsTraceFile,
                                true,
                                "AlignToTime",
                                timeToAlign,
                                false,
                                false,
                            ),
                        )
                    }
                }
                contents {
                    "test"(source)
                    "g"(Double.POSITIVE_INFINITY)
                }
            }
        }
        if (mode is LaunchMode.Batch) {
            terminator(AfterTime(DoubleTime(mode.maxTime)))
        }
    }
    if (mode is LaunchMode.Gui) {
        monitor(SwingGUI(environment, "effects/helloScafi.json"))
    }
    exportWith(CSVExporter("experiment", exportInterval, "build/exports/helloScafi", "txt")) {
        -Time()
        -NumberOfNodes()
        -moleculeReader("g", null, CommonFilters.ONLYFINITE.filteringPolicy, listOf("sum"))
        -moleculeReader("g", null, CommonFilters.ONLYFINITE.filteringPolicy, listOf("mean"))
    }
}
