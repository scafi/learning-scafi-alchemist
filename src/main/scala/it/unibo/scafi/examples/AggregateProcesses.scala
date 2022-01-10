package it.unibo.scafi.examples

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import it.unibo.scafi.utils.MovementUtils

class AggregateProcesses extends AggregateProgram with StandardSensors with ScafiAlchemistSupport
  with CustomSpawn with Gradients with MovementUtils {
  import SpawnInterface._
  import AggregateProcesses._

  def processes_spec: Map[Int,(Int,Int)] = Map(0 -> (0,50), 20 -> (1,100), 30 -> (390,50), 25 -> (190, 35), 80 -> (380, 85))

  /**
   * This program realises simple spawning of gradient processes.
   * The "difficult" part lies in controlling the processes' lifecycle.
   */
  override def main(): Any = {
    // Determine the processes to be generated (these are provided in a molecule "procs")
    val procs: Set[ProcessSpec] = /* node.get[Map[Int,(Int,Int)]](MOLECULE_PROCS)*/ processes_spec.map(tp => ProcessSpec.fromTuple(tp._1, tp._2)).toSet
    val t = alchemistTimestamp.toDouble.toLong
    val pids: Set[Pid] = procs.filter(tgen => tgen.device == mid() && t > tgen.startTime && (t - 5) < tgen.startTime)
      .map(tgen => Pid(time = tgen.startTime)(terminateAt = tgen.endTime))

    val maps = sspawn[Pid,Unit,Double](process, pids, {})

    if(!maps.isEmpty) {
      node.put(EXPORT_PID, Math.abs(maps.maxBy(_._1.time)._1.hashCode()) % 100)
      node.put(EXPORT_G, maps.maxBy(_._1.time)._2)
    } else { removeMolecule(EXPORT_PID); removeMolecule(EXPORT_G); }

    rectangleWalk()
    node.put(EXPORT_PROCESS_KEYSET, maps.keySet)
    node.put(EXPORT_NUMBER_OF_PROCESSES, maps.size)
  }

  // TODO: fix remove to perform the check
  def removeMolecule(name: String) = if(node.has(name)) node.remove(name)

  case class Pid(src: ID = mid(), time: Long = alchemistTimestamp.toDouble.toLong)
                (val terminateAt: Long = Long.MaxValue)

  def process(pid: Pid)(src: Unit={}): POut[Double] = {
    val g = classicGradient(pid.src==mid())
    val s = if(pid.src==mid() && pid.terminateAt.toDouble <= alchemistTimestamp.toDouble){
      Terminated
    } else if(g < 200) Output else External
    POut(g, s)
  }

  /*
  override def sspawn[A, B, C](process: A => B => POut[C], params: Set[A], args: B): Map[A,C] = {
    spawn[A,B,Option[C]]((p: A) => (a: B) => {
      val firstTime = rep(0)(_ + 1) == 1
      val (finished, result, status) = rep((false, none[C], false)) { case (finished, _, _) => {
        val POut(result, status) = process(p)(a)
        val newFinished = status == Terminated | includingSelf.anyHood(nbr{finished})
        if(newFinished){ println(s"${mid()} finishing") }
        val terminated = (firstTime && newFinished) | includingSelf.everyHood(nbr{newFinished})
        if(newFinished){ println(s"${mid()} terminating as everybody else has finished") }
        val (newResult, newStatus) = (result, status) match {
          case _ if terminated     => (None, false)
          case (_,     External)   => (None, false)
          case (_,     Terminated) => (None, true)
          case (value, Output)     => (Some(value), true)
          case (_,     Bubble)     => (None, true)
        }
        (newFinished, newResult, newStatus)
      } }
      //val exitTuple = s"""exit("${p}")"""
      //if(!status){ addTupleIfNotAlreadyThere(exitTuple) } else { removeTuple(exitTuple) }
      (result, status)
    }, params, args).collect { case (k, Some(p)) => k -> p }
  }
  def none[T]: Option[T] = None

   */
}

object AggregateProcesses {
  val MOLECULE_PROCS = "procs"

  val EXPORT_PID = "pid"
  val EXPORT_PROCESS_KEYSET = "pids"
  val EXPORT_NUMBER_OF_PROCESSES = "numPids"
  val EXPORT_G = "g"

  case class ProcessSpec(startTime: Int, device: Int, endTime: Int)
  object ProcessSpec {
    implicit def fromTuple(time: Int, details: (Int,Int)) = ProcessSpec(time, details._1, details._2)
  }
}