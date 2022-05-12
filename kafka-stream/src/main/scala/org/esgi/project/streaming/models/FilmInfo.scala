// Nombre de vues par film, catégorisées par type de vue

package org.esgi.project.streaming.models

import play.api.libs.json.{Json, OFormat}

case class FilmInfo(
                     title: String,
                     view_count: Long,
                     start_only: Long,
                     half: Long,
                     full: Long
                   ) {

  def increment_view_count(view_category: String) =
  {
    view_category match {
      case "half" => this.copy(half = this.half + 1, view_count = this.view_count + 1)
      case "full" => this.copy(full = this.full + 1, view_count = this.view_count + 1)
      case "start_only" => this.copy(start_only = this.start_only + 1, view_count = this.view_count + 1)
      case _ => this.copy()
    }
  }
  def set_title(t: String) = this.copy(title = t)
}
object FilmInfo {
  implicit val format: OFormat[FilmInfo] = Json.format[FilmInfo]
  def empty: FilmInfo = FilmInfo(
    half = 0,
    full = 0,
    start_only = 0,
    title = "",
    view_count = 0)
}