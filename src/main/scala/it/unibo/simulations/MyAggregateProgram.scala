package it.unibo.simulations

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._

class MyAggregateProgram extends AggregateProgram
  with StandardSensors with ScafiAlchemistSupport with BlockG with BlockC with BlockS with FieldUtils {

  override type MainResult = Any

  override def main = {
    1+1
  }

}