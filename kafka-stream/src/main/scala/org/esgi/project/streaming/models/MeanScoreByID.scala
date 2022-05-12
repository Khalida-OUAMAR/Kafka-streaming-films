package org.esgi.project.streaming.models

import play.api.libs.json.{Json, OFormat}
case class MeanScoreByID(
                          sum: Double,
                          count: Double,
                          meanScore: Double
                        ) {
  def increment(score: Double) = this.copy(sum = this.sum + score, count = this.count + 1)

  def computeMeanScore = this.copy(
    meanScore = this.sum / this.count
  )
}

object MeanScoreByID {
  implicit val format: OFormat[MeanScoreByID] = Json.format[MeanScoreByID]

  def empty: MeanScoreByID = MeanScoreByID(0, 0, 0)
}