package org.esgi.project.api

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.apache.kafka.streams.{KafkaStreams, StoreQueryParameters}
import org.apache.kafka.streams.state.{QueryableStoreTypes, ReadOnlyKeyValueStore, ReadOnlyWindowStore, WindowStoreIterator}
import org.esgi.project.api.models.{NbViewById, ScoreResponse, Stat, ViewResponse}
import org.esgi.project.streaming.StreamProcessing
import org.esgi.project.streaming.models.FilmInfo

import java.time.{Instant}
import scala.jdk.CollectionConverters._


object WebServer extends PlayJsonSupport {
  def routes(streams: KafkaStreams): Route = {
    concat(
      path("movies" / Segment) { id: String =>
        get {
          val FilmId = id.toLong
          print(FilmId)

          val begin1m = Instant.now().minusSeconds(60)
          val end1m = begin1m.plusSeconds(1)


          val begin5m = Instant.now().minusSeconds(300)
          val end5m = begin5m.plusSeconds(1)
          // 1 minutes
          val kvStoreLastMinute: ReadOnlyWindowStore[Long, FilmInfo] = streams
            .store(StoreQueryParameters
              .fromNameAndType(StreamProcessing.viewLastMinuteStore,
                QueryableStoreTypes.windowStore[Long, FilmInfo]()))

          val viewLastMinute: FilmInfo = kvStoreLastMinute
            .fetch(
              FilmId,
              begin1m,
              end1m).asScala.toList.headOption.map(_.value).getOrElse(FilmInfo.empty)

          val kvStoreLast5Minute: ReadOnlyWindowStore[Long, FilmInfo] = streams
            .store(StoreQueryParameters
              .fromNameAndType(StreamProcessing.viewLast5MinutesStore,
                QueryableStoreTypes.windowStore[Long, FilmInfo]()))

          val viewLast5Minutes = kvStoreLast5Minute
            .fetch(
              FilmId,
              begin5m,
              end5m).asScala.toList.headOption.map(_.value).getOrElse(FilmInfo.empty)

          val kvStorePast: ReadOnlyKeyValueStore[Long, FilmInfo] = streams
            .store(StoreQueryParameters
              .fromNameAndType(StreamProcessing.storeMovieName,
                QueryableStoreTypes.keyValueStore[Long, FilmInfo]()))

          val viewPast = kvStorePast.get(FilmId)

          val lastMinute = Stat(viewLastMinute.start_only, viewLastMinute.half, viewLastMinute.full)
          val lastFiveMinutes = Stat(viewLast5Minutes.start_only, viewLast5Minutes.half, viewLast5Minutes.full)
          val past = Stat(viewPast.start_only, viewPast.half, viewPast.full)

          val statistics = Map(
            "past" -> past,
            "last_minute" -> lastMinute,
            "last_five_minutes" -> lastFiveMinutes)
          complete(
            NbViewById(FilmId, viewLastMinute.title, viewPast.view_count, statistics)
          )
        }},
      path("/stats/ten/best/" / Segment) { stat: String =>
        get {
          stat match {
            case "score" =>
              val kvStoreScore: ReadOnlyWindowStore[String, Long] = streams
                .store(
                  StoreQueryParameters.fromNameAndType(
                    StreamProcessing.BestScore,
                    QueryableStoreTypes.windowStore[String, Long]()
                  )
                )
              val keys =
                kvStoreScore.all().asScala.map(_.key.key()).toList.distinct

              complete(
                keys.map((key) =>
                  ScoreResponse(
                    key,
                    kvStoreScore
                      .all()
                      .asScala
                      .toList
                      .headOption
                      .map(_.value)
                      .getOrElse(0)
                  )
                )
              )
            case "views" =>
              val kvStoreView: ReadOnlyWindowStore[String, Long] = streams
                .store(
                  StoreQueryParameters.fromNameAndType(
                    StreamProcessing.BestView,
                    QueryableStoreTypes.windowStore[String, Long]()
                  )
                )
              val keys =
                kvStoreView.all().asScala.map(_.key.key()).toList.distinct

              complete(
                keys.map((key) =>
                  ViewResponse(
                    key,
                    kvStoreView
                      .all()
                      .asScala
                      .toList
                      .headOption
                      .map(_.value)
                      .getOrElse(0)
                  )
                )
              )
          }
        }
      },
      path("/stats/ten/worst/" / Segment) { stat: String =>
        get {
          stat match {
            case "score" =>
              val kvStoreScore: ReadOnlyWindowStore[String, Long] = streams
                .store(
                  StoreQueryParameters.fromNameAndType(
                    StreamProcessing.WorstScore,
                    QueryableStoreTypes.windowStore[String, Long]()
                  )
                )
              val keys =
                kvStoreScore.all().asScala.map(_.key.key()).toList.distinct

              complete(
                // TODO: output a list of VisitCountResponse objects
                keys.map((key) =>
                  ScoreResponse(
                    key,
                    kvStoreScore
                      .all()
                      .asScala
                      .toList
                      .headOption
                      .map(_.value)
                      .getOrElse(0)
                  )
                )
              )
            case "views" =>
              val kvStoreView: ReadOnlyWindowStore[String, Long] = streams
                .store(
                  StoreQueryParameters.fromNameAndType(
                    StreamProcessing.WorstView,
                    QueryableStoreTypes.windowStore[String, Long]()
                  )
                )
              val keys =
                kvStoreView.all().asScala.map(_.key.key()).toList.distinct

              complete(
                keys.map((key) =>
                  ViewResponse(
                    key,
                    kvStoreView
                      .all()
                      .asScala
                      .toList
                      .headOption
                      .map(_.value)
                      .getOrElse(0)
                  )
                )
              )
          }
        }
      }
    )
  }
}
