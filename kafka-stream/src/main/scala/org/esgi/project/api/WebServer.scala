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

import java.time.{Instant, OffsetDateTime}
import scala.jdk.CollectionConverters._


object WebServer extends PlayJsonSupport {
  def routes(streams: KafkaStreams): Route = {
    concat(
      path("movies" / Segment) { id: String =>
        get {

          val kvStoreMovies: ReadOnlyKeyValueStore[Long, FilmInfo] = streams
            .store(StoreQueryParameters
              .fromNameAndType(StreamProcessing.storeMovieName,
              QueryableStoreTypes.keyValueStore[Long, FilmInfo]()))

          val FilmId = id.toLong
          val FilmInfo = kvStoreMovies.get(FilmId)

          // now
          val viewPast: ReadOnlyKeyValueStore[(Long, String), Long] = streams
            .store(StoreQueryParameters.fromNameAndType(StreamProcessing.viewPast,
              QueryableStoreTypes.keyValueStore[(Long, String), Long]()))

          val viewPastStartOnly: Long = viewPast.get((FilmId, "start_only"))
          val viewPastHalf: Long = viewPast.get((FilmId, "half"))
          val viewPastFull: Long = viewPast.get((FilmId, "full"))

          val begin1m = Instant.now().minusSeconds(60)
          val end1m = begin1m.minusSeconds(1)


          val begin5m = Instant.now().minusSeconds(300)
          val end5m = begin5m.minusSeconds(1)


          // 1 minutes
          val kvStoreLastMinute: ReadOnlyWindowStore[(Long, String), Long] = streams
            .store(StoreQueryParameters
              .fromNameAndType(StreamProcessing.viewLastMinute,
                QueryableStoreTypes.windowStore[(Long, String), Long]()))

          val viewLastMinuteStartOnly = kvStoreLastMinute
            .fetch(
              (FilmId, "start_only"),
              begin1m,
              end1m).asScala.toList.head.value

          val viewLastMinuteHalf = kvStoreLastMinute
            .fetch(
              (FilmId, "half"),
              begin1m,
              end1m).asScala.toList.head.value

          val viewLastMinuteFull = kvStoreLastMinute
            .fetch(
              (FilmId, "full"),
              begin1m,
              end1m).asScala.toList.head.value

          // last 5 minutes
          val kvStoreLastFiveMinutes: ReadOnlyWindowStore[(Long, String), Long] = streams
            .store(StoreQueryParameters
              .fromNameAndType(StreamProcessing.viewLast5Minutes,
                QueryableStoreTypes.windowStore[(Long, String), Long]())
            )

          val viewLastFiveMinuteStartOnly = kvStoreLastFiveMinutes
            .fetch((FilmId, "start_only"),
              begin5m,
              end5m).asScala.toList.head.value

          val viewLastFiveMinuteHalf = kvStoreLastFiveMinutes
            .fetch((FilmId, "half"),
            begin5m,
            end5m).asScala.toList.head.value

          val viewLastFiveMinuteFull = kvStoreLastFiveMinutes
            .fetch((FilmId, "full"),
              begin5m,
              end5m).asScala.toList.head.value

          val past = Stat(viewPastStartOnly, viewPastHalf, viewPastFull)
          val lastMinute = Stat(viewLastMinuteStartOnly, viewLastMinuteHalf, viewLastMinuteFull)
          val lastFiveMinutes = Stat(viewLastFiveMinuteStartOnly, viewLastFiveMinuteHalf, viewLastFiveMinuteFull)

          val statistics = Map("past" -> past,
                              "last_minute" -> lastMinute,
                              "last_five_minutes" -> lastFiveMinutes)
          complete(
            NbViewById(FilmId, FilmInfo.title, FilmInfo.view_count, statistics)
          )
          }
      }
    )
  }
}
