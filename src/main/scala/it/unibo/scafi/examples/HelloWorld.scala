package it.unibo.scafi.examples

import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._

import scala.concurrent.duration.FiniteDuration

class HelloWorld extends AggregateProgram with StandardSensors with ScafiAlchemistSupport
  with Gradients with FieldUtils {
  override def main(): Any = {
    checkSensors()

    // Access to node state through "molecule"
    val x = if(node.has("prova")) node.get[Int]("prova") else 1
    // An aggregate operation
    val g = classicGradient(mid()==100)
    // Write access to node state
    node.put("g", g)
    // Return value of the program
    g
  }

  def checkSensors() = {
    val timestamp: it.unibo.scafi.time.TimeAbstraction#Time = currentTime()
    val delta: FiniteDuration = deltaTime()
    val nbrLagField = includingSelf.reifyField(nbrLag())
    val nbrRangeField = includingSelf.reifyField(nbrRange())
    val nbrVectorField = includingSelf.reifyField(nbrVector())
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