package it.unibo.scafi.utils

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import it.unibo.scafi.space.{Point2D, Point3D}

import scala.language.implicitConversions

trait MovementUtils {
  self: AggregateProgram with StandardSensors with ScafiAlchemistSupport =>

  def rectangleWalk(p1: Point2D = Point2D(0,0), p2: Point2D = Point2D(1000,1000), molecule: String = "target"): Unit = {
    val goal = randomPoint()
    node.put(molecule, ifClose(cropRectangle(goal, p1, p2)))
  }

  def cropRectangle(goal: Point2D, rect1: Point2D, rect2: Point2D): Point2D = {
    Point2D(goal.x.max(rect1.x).min(rect2.x), goal.y.max(rect1.y).min(rect2.y))
  }

  def randomPoint(p: Point3D = currentPosition(), maxStep: Double = 25): Point2D = {
    Point2D(p.x + (maxStep * 2) * (nextRandom()-0.5), p.y + (maxStep * 2) * (nextRandom()-0.5))
  }

  def ifClose(goal: Point3D, dist: Double = 1): Point2D = {
    rep(goal)(g => if(currentPosition().distance(g) <= dist) goal else g )
  }

  implicit class RichPoint3D(p: Point3D) {
    def toAlchemistPosition: P = alchemistEnvironment.makePosition(p.productIterator.map(_.asInstanceOf[Number]).toSeq:_*).asInstanceOf[P]
  }

  implicit def toPoint2D(p: Point3D): Point2D = Point2D(p.x, p.y)
}
