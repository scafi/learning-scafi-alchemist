package it.unibo.casestudy

import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._

class HelloWorld extends AggregateProgram with StandardSensors with Gradients with ScafiAlchemistSupport
  with FieldCalculusSyntax with FieldUtils with CustomSpawn {
  override def main(): Any = {
    //val x = node.get[Int]("prova")
    //node.put("prova2", x+1)
    //classicGradient(mid==100)

    val source = mid % 250 == 0

    def process_logic(procId: Int)(stillSource: Boolean): (Double, Boolean) = {
      val g = classicGradient(mid==procId && stillSource)
      (g, g < 2000 && (mid!=procId || stillSource))
    }

    val procs = spawn[Int,Boolean,Double]((process_logic _), if(source) Set(mid) else Set.empty, source)
      .toList.sortBy(_._2)

    def procName(i: Int) = s"proc${i}"
    val thisNode = alchemistEnvironment.getNodeByID(mid)
    import scala.collection.JavaConverters._
    thisNode.getContents.asScala.foreach( tp => if(tp._1.getName.startsWith("proc")) thisNode.removeConcentration(tp._1) )
    procs.foreach {
      case (pid,value) => node.put(procName(pid), 1)
    }
  }
}