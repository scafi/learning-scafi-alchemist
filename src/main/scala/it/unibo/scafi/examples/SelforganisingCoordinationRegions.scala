package it.unibo.scafi.examples

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import it.unibo.scafi.utils.MovementUtils

class SelforganisingCoordinationRegions extends AggregateProgram with StandardSensors with ScafiAlchemistSupport
  with Gradients with BlockS  with BlockG with BlockC {
  import SelforganisingCoordinationRegions._

  /**
   * This program realises a simple implementation of the Self-organising Coordination Regions (SCR) pattern.
   */
  override def main(): Any = {
    // Sparse choice (leader election) of the cluster heads
    val leader = S(sense(Params.GRAIN), metric = nbrRange)
    // G block to run a gradient from the leaders
    val g = distanceTo(leader, metric = nbrRange)
    // C block to collect information towards the leaders
    val c = C[Double,Set[ID]](g, _++_, Set(mid()), Set.empty)
    // G block to propagate decisions or aggregated info from leaders to members
    val info = G[Set[ID]](leader, c, identity, metric = nbrRange)
    val head = G[ID](leader, mid(), identity, metric = nbrRange)

    // Just let every node export some data
    node.put(Exports.LEADER, if(leader) 1 else 0)
    node.put(Exports.GRADIENT, g)
    node.put(Exports.INCLUDED, if(leader) 1 else info.contains(mid()))
    node.put(Exports.COUNT, if(leader) c.size else 0)
    node.put(Exports.ISSUES, excludingSelf.anyHood(nbr { head } == head & nbr { info } != info))
  }

}
object SelforganisingCoordinationRegions {
  object Exports {
    val LEADER = "leader"
    val INCLUDED = "included"
    val COUNT = "count"
    val GRADIENT = "g"
    val ISSUES = "issues"
  }
  object Params {
    val GRAIN = "grain"
  }
}

