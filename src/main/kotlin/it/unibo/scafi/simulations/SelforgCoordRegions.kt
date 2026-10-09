package it.unibo.scafi.simulations

import it.unibo.alchemist.boundary.exporters.CSVExporter
import it.unibo.alchemist.boundary.exportfilters.CommonFilters
import it.unibo.alchemist.boundary.extractors.NumberOfNodes
import it.unibo.alchemist.boundary.extractors.Time
import it.unibo.alchemist.boundary.extractors.moleculeReader
import it.unibo.alchemist.boundary.kotlindsl.simulation2D
import it.unibo.alchemist.boundary.swingui.monitor.impl.SwingGUI
import it.unibo.alchemist.boundary.variables.LinearVariable
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.deployments.grid
import it.unibo.alchemist.model.environments.continuous2DEnvironment
import it.unibo.alchemist.model.incarnations.ScafiIncarnation
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.reactions.event
import it.unibo.alchemist.model.scafi.actions.RunScafiProgram
import it.unibo.alchemist.model.terminators.AfterTime
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.model.times.DoubleTime
import org.apache.commons.math3.random.RandomGenerator

/**
 * Kotlin DSL equivalent of `src/main/yaml/selforgCoordRegions.yml`.
 */
fun selforgCoordRegions(mode: LaunchMode) = simulation2D(ScafiIncarnation<Any, Euclidean2DPosition>()) {
    val random by variable(LinearVariable(default = 2.0, min = 0.0, max = 5.0, step = 1.0))
    val range = 100.0
    val grain = 500.0
    val retentionTime = 5.0
    val exportInterval = 1.0
    scenarioSeed(random.toLong())
    simulationSeed(random.toLong())
    val environment = continuous2DEnvironment<Any>()
    environment(environment) {
        networkModel(ConnectWithinDistance(range))
        deployments {
            deploy(grid(contextOf<RandomGenerator>(), 0.0, 0.0, 1000.0, 1000.0, 50.0, 50.0, 25.0, 25.0)) {
                val node = contextOf<Node<Any>>()
                // _reactions.program
                withTimeDistribution(DiracComb<Any>(DoubleTime(0.0), 1.0)) {
                    program(event()) {
                        action(
                            RunScafiProgram<Any, Euclidean2DPosition>(
                                environment,
                                node,
                                contextOf<Reaction<Any>>(),
                                contextOf<RandomGenerator>(),
                                "it.unibo.scafi.examples.SelforganisingCoordinationRegions",
                                retentionTime,
                            ),
                        )
                    }
                }
                program("send")
                contents {
                    "grain"(grain)
                }
            }
        }
        if (mode is LaunchMode.Batch) {
            terminator(AfterTime(DoubleTime(mode.maxTime)))
        }
    }
    if (mode is LaunchMode.Gui) {
        monitor(SwingGUI(environment, "effects/selforgCoordRegions.json"))
    }
    exportWith(CSVExporter("experiment", exportInterval, "build/exports/selforgCoordRegions", "txt")) {
        -Time()
        -NumberOfNodes()
        -moleculeReader("leader", null, CommonFilters.ONLYFINITE.filteringPolicy, listOf("sum"))
        -moleculeReader("included", null, CommonFilters.ONLYFINITE.filteringPolicy, listOf("sum"))
        -moleculeReader("count", null, CommonFilters.ONLYFINITE.filteringPolicy, listOf("sum", "mean"))
        -moleculeReader("issues", null, CommonFilters.ONLYFINITE.filteringPolicy, listOf("sum"))
    }
}
