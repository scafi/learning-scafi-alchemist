package it.unibo.scafi.simulations

import it.unibo.alchemist.boundary.exporters.CSVExporter
import it.unibo.alchemist.boundary.exportfilters.CommonFilters
import it.unibo.alchemist.boundary.extractors.Time
import it.unibo.alchemist.boundary.extractors.moleculeReader
import it.unibo.alchemist.boundary.kotlindsl.simulation2D
import it.unibo.alchemist.boundary.swingui.monitor.impl.SwingGUI
import it.unibo.alchemist.boundary.variables.LinearVariable
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.actions.moveToTarget
import it.unibo.alchemist.model.cognitive.properties.pedestrian
import it.unibo.alchemist.model.cognitive.reactions.blendedSteering
import it.unibo.alchemist.model.deployments.grid
import it.unibo.alchemist.model.environments.continuous2DEnvironment
import it.unibo.alchemist.model.incarnations.ScafiIncarnation
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.reactions.event
import it.unibo.alchemist.model.scafi.actions.RunScafiProgram
import it.unibo.alchemist.model.terminators.AfterTime
import it.unibo.alchemist.model.timedistributions.exponentialTime
import it.unibo.alchemist.model.times.DoubleTime
import org.apache.commons.math3.random.RandomGenerator

/**
 * Kotlin DSL equivalent of `src/main/yaml/aggregateProcesses.yml`.
 */
fun aggregateProcesses(mode: LaunchMode) = simulation2D(ScafiIncarnation<Any, Euclidean2DPosition>()) {
    val random by variable(LinearVariable(default = 2.0, min = 0.0, max = 5.0, step = 1.0))
    val connectionRange = 100.0
    val retentionTime = 5.0
    val programRate = 1.0
    val movementRate = 10.0
    val movementSpeed = 50.0
    val exportInterval = 3.0
    val procs = "scala.collection.Map(0 -> (0,50), 20 -> (1,100), 30 -> (390,50), 25 -> (190, 35), 80 -> (380, 85))"
    // Seeds must be set before the environment
    scenarioSeed(random.toLong())
    simulationSeed(random.toLong())
    val environment = continuous2DEnvironment<Any>()
    environment(environment) {
        networkModel(ConnectWithinDistance(connectionRange))
        deployments {
            deploy(grid(contextOf<RandomGenerator>(), 0.0, 0.0, 1000.0, 1000.0, 50.0, 50.0, 5.0, 5.0)) {
                val node = contextOf<Node<Any>>()
                // properties: [Pedestrian]
                val pedestrian = pedestrian(node)
                nodeProperty(pedestrian)
                // _reactions.move
                withTimeDistribution(movementRate) {
                    program(blendedSteering(environment, pedestrian)) {
                        action(moveToTarget(node, contextOf<Reaction<Any>>(), SimpleMolecule("target"), movementSpeed))
                    }
                }
                // _reactions.program
                withTimeDistribution(exponentialTime<Any>(programRate)) {
                    program(event()) {
                        action(
                            RunScafiProgram<Any, Euclidean2DPosition>(
                                environment,
                                node,
                                contextOf<Reaction<Any>>(),
                                contextOf<RandomGenerator>(),
                                "it.unibo.scafi.examples.AggregateProcesses",
                                retentionTime,
                            ),
                        )
                    }
                }
                program("send")
                contents {
                    "test"(true)
                    "g"(Double.POSITIVE_INFINITY)
                    "numPids"(0)
                    "procs"(concentrationOf(procs))
                }
            }
        }
        if (mode is LaunchMode.Batch) {
            terminator(AfterTime(DoubleTime(mode.maxTime)))
        }
    }
    if (mode is LaunchMode.Gui) {
        monitor(SwingGUI(environment, "effects/aggregateProcesses.json"))
    }
    exportWith(CSVExporter("experiment", exportInterval, "build/exports/aggregateProcesses", "txt")) {
        -Time()
        -moleculeReader("numPids", null, CommonFilters.ONLYFINITE.filteringPolicy, listOf("sum"))
    }
}
