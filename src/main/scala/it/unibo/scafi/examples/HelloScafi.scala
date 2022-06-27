package it.unibo.scafi.examples

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._

import scala.concurrent.duration.FiniteDuration

class HelloScafi extends AggregateProgram with StandardSensors with ScafiAlchemistSupport
  with BlockG with Gradients with FieldUtils {
  override def main(): Any = {
    checkSensors()
    // Access to node state through "molecule"
    val source = sense[Int]("test") // Alchemist API => node.get("test")
    // An aggregate operation
    val g = classicGradient(mid() == source)
    // Write access to node state (i.e., Actuation => it changes the node state)
    node.put("g", g)
    // Return value of the program
    g
  }

  /* operation on neighbours */
  def checkSensors(): Unit = {
    val timestamp: it.unibo.scafi.time.TimeAbstraction#Time = currentTime()
    val delta: FiniteDuration = deltaTime()
    // reifyField creates a map of values gathered from the neighbours
    val nbrLagField = includingSelf.reifyField(nbrLag())
    val nbrRangeField = includingSelf.reifyField(nbrRange())
    val nbrVectorField = includingSelf.reifyField(nbrVector())
    // "Actuation" => put the information evaluated by this program inside a "molecule" (i.e., an alchemist variable)
    node.put("sensorData",
      s"""
      | timestamp = $timestamp
      | deltaTime = $delta
      | nbrLagField = $nbrLagField
      | nbrRange = $nbrRangeField
      | nbrVector = $nbrVectorField
      |
      |""".stripMargin)
  }
}