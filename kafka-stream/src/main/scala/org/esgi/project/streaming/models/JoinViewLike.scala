package org.esgi.project.streaming.models

import play.api.libs.json.{Json, OFormat}

case class JoinViewLike(
                           _id: Long,
                           title: String,
                           view_category: String,
                           score: Double
                         )



object JoinViewLike {
  implicit val format: OFormat[JoinViewLike] = Json.format[JoinViewLike]
}
