package org.esgi.project.api

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.state.{QueryableStoreTypes, ReadOnlyKeyValueStore, ReadOnlyWindowStore, WindowStoreIterator}
import org.esgi.project.api.models.{MeanLatencyForURLResponse, VisitCountResponse}
import org.esgi.project.streaming.StreamProcessing
import org.esgi.project.streaming.models.MeanLatencyForURL

import java.time.Instant
import scala.jdk.CollectionConverters._

/**
 * -------------------
 * Part.3 of exercise: Interactive Queries
 * -------------------
 */
object WebServer extends PlayJsonSupport {
  def routes(streams: KafkaStreams): Route = {
    concat(
      path("/stats/ten/best/" / Segment) { stat: String =>
        get {
          stat match {
            case "score" =>
              // TODO: load the store containing the visits count of the last 30 seconds and query it to
              // TODO: fetch the keys of the last window and its info
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
              // TODO: load the store containing the visits count of the last minute and query it to
              // TODO: fetch the keys of the last window and its info
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
                // TODO: output a list of VisitCountResponse objects
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
              // TODO: load the store containing the visits count of the last 30 seconds and query it to
              // TODO: fetch the keys of the last window and its info
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
              // TODO: load the store containing the visits count of the last minute and query it to
              // TODO: fetch the keys of the last window and its info
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
                // TODO: output a list of VisitCountResponse objects
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
