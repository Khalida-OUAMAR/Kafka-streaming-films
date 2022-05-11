// Nombre de vues par film, catégorisées par type de vue

package org.esgi.project.streaming.models

import play.api.libs.json.{Json, OFormat}

case class FilmInfo(
                    title: String,
                    view_count: Long
                   ) {

}
object FilmInfo {
  implicit val format: OFormat[FilmInfo] = Json.format[FilmInfo]
}
