package org.esgi.project.api.models

import play.api.libs.json.{Json, OFormat}

case class Stat(
                start_only: Long,
                half: Long,
                full: Long)


object Stat {
  implicit val format: OFormat[Stat] = Json.format[Stat]
}
