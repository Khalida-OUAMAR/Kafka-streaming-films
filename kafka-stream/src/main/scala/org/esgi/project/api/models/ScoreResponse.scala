package org.esgi.project.api.models

import play.api.libs.json.{Json, OFormat}

case class ScoreResponse(
                 title: String,
                 score: Long)


object ScoreResponse {
  implicit val format: OFormat[ScoreResponse] = Json.format[ScoreResponse]
}
