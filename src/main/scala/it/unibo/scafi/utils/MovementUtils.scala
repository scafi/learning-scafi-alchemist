package it.unibo.scafi.utils

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
import it.unibo.scafi.space.{Point2D, Point3D}

trait MovementUtils {
  self: AggregateProgram with StandardSensors with ScafiAlchemistSupport =>

  def rectangleWalk(p1: Point2D = Point2D(0,0), p2: Point2D = Point2D(1000,1000), molecule: String = "target"): Unit = {
    val goal = randomPoint()
    node.put(molecule, ifClose(cropRectangle(goal, p1, p2)))
  }

  def cropRectangle(goal: Point2D, rect1: Point2D, rect2: Point2D): Point2D = {
    Point2D(if(goal.x < rect1.x) rect1.x else if(goal.x > rect2.x) rect2.x else goal.x,
      if(goal.y < rect1.y) rect1.y else if(goal.y > rect2.x) rect2.y else goal.y)
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
