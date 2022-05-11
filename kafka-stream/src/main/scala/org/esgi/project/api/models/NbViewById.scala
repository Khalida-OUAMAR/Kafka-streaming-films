package org.esgi.project.api.models

import play.api.libs.json.{Json, OFormat}
import org.esgi.project.api.models.Stat

import java.util.Dictionary

case class NbViewById(
                               _id: Long,
                               title: String,
                               view_count: Long,
                               Stats: Map[String, Stat]
                             )

object NbViewById {
  implicit val format: OFormat[NbViewById] = Json.format[NbViewById]
}
