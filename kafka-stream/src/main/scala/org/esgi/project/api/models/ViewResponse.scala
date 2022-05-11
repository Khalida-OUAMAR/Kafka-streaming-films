package org.esgi.project.api.models

import play.api.libs.json.{Json, OFormat}

case class ViewResponse(
                          title: String,
                          views: Long)


object ViewResponse {
  implicit val format: OFormat[ViewResponse] = Json.format[ViewResponse]
}
