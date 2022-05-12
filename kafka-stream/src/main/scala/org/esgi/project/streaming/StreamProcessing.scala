package org.esgi.project.streaming

import io.github.azhur.kafkaserdeplayjson.PlayJsonSupport
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.kstream.{JoinWindows, TimeWindows, Windowed}
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream._
import org.esgi.project.streaming.models.{JoinViewLike, Like, MeanScoreByID, View, FilmInfo}
import java.io.InputStream
import java.time.Duration
import java.util.Properties

object StreamProcessing extends PlayJsonSupport {

  import org.apache.kafka.streams.scala.ImplicitConversions._
  import org.apache.kafka.streams.scala.serialization.Serdes._

  // Predeclared store names to be used, fill your first & last name
  val yourFirstName: String = "Amandidne"
  val yourLastName: String = "Thivet"

  val applicationName = s"web-events-stream-app-$yourFirstName-$yourLastName"
  val viewsTopicName: String = "views"
  val likesTopicName: String = "likes"
  val joinViewLikeTopicName: String = "joinviewlike"

  // Declaration of store's names

  val storeMovieName: String = "storeMovieName"
  val movieMeanScoreStore: String = "movieMeanScoreStore"

  val viewStartStore: String = "viewStartStore"
  val viewLastMinuteStore: String = "viewLastMinuteStore"
  val viewLast5MinutesStore: String = "viewLast5MinutesStore"

  val movieBestScoreStore: String = "movieBestScoreStore"
  val movieMoreViewStore: String = "movieMoreViewStore"
  val movieWorstScoreStore: String = "movieWorstScoreStore"
  val movieLessViewStore: String = "movieLessViewStore"

  val meanScorePerMovieStore: String = "meanScorePerMovieStore"
  val WorstView: String = "WorstView"
  val viewPast: String = "viewPast"
  val BestScore: String = "BestScore"
  val BestView: String = "BestView"
  val WorstScore: String = "WorstScore"

  val props = buildProperties

  // defining processing graph
  val builder: StreamsBuilder = new StreamsBuilder

  val views: KStream[String, View] = builder.stream[String, View](viewsTopicName)
  val likes: KStream[String, Like] = builder.stream[String, Like](likesTopicName)

  /**
   * -----------------
   * Global analyse : Nb views per movie, repartition views per movie
   * -----------------
   */



  val viewGroupedByMovie: KGroupedStream[Long, View] = {
    views.groupBy((_, value) => value._id)
  }


  val viewLastMinute: KTable[Windowed[Long], FilmInfo] = viewGroupedByMovie
    .windowedBy(
      TimeWindows.of(Duration.ofMinutes(1)).advanceBy(Duration.ofSeconds(1))
    )
    .aggregate(FilmInfo.empty)(
      (_,v,agg)=>agg.increment_view_count(v.view_category).set_title(v.title)
    )(Materialized.as(viewLastMinuteStore))

  val viewLast5Minutes: KTable[Windowed[Long], FilmInfo] = viewGroupedByMovie
    .windowedBy(
      TimeWindows.of(Duration.ofMinutes(5)).advanceBy(Duration.ofSeconds(1))
    )
    .aggregate(FilmInfo.empty)(
      (_,v,agg)=>agg.increment_view_count(v.view_category)
        .set_title(v.title)
    )(Materialized.as(viewLast5MinutesStore))

  val viewPastTable: KTable[Long, FilmInfo] = viewGroupedByMovie
    .aggregate(FilmInfo.empty)(
      (_,v,agg)=>agg.increment_view_count(v.view_category).set_title(v.title)
    )(Materialized.as(storeMovieName))

  /**
   * -------------------
   * Ratings
   * -------------------
   */

  // Join View Like
  val joinViewsWithLikes: KStream[String, JoinViewLike] = views
    .join(likes)(
      { (view, like) =>
        JoinViewLike(
          _id = view._id,
          title = view.title,
          view_category = view.view_category,
          score = like.score
        )
      },
      JoinWindows.of(Duration.ofSeconds(5))
    )

  // Recup liste avec id  pour l'agg
  val viewsLikesGroupById: KGroupedStream[Long, JoinViewLike] = joinViewsWithLikes.groupBy((_, joinViewsWithLikes) => joinViewsWithLikes._id)


  // Mean score
  val movieMeanScore:  KTable[Long, MeanScoreByID] = joinViewsWithLikes
    .map((_, joinViewLike) => (joinViewLike._id, joinViewLike))
    .groupByKey
    .aggregate(MeanScoreByID.empty) {
      (_, newJoinViewLike, accumulator) =>
        accumulator
          .increment(score = newJoinViewLike.score)
          .computeMeanScore
    }(Materialized.as(movieMeanScoreStore))


  def run(): KafkaStreams = {
    val streams: KafkaStreams = new KafkaStreams(builder.build(), props)
    streams.start()

    // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable() {
      override def run {
        streams.close
      }
    }))
    streams
  }

  // auto loader from properties file in project
  def buildProperties: Properties = {
    import org.apache.kafka.clients.consumer.ConsumerConfig
    import org.apache.kafka.streams.StreamsConfig
    val inputStream: InputStream = getClass.getClassLoader.getResourceAsStream("kafka.properties")

    val properties = new Properties()
    properties.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationName)
    // Disable caching to print the aggregation value after each record
    properties.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0")
    properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")
    properties.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, "-1")
    properties.load(inputStream)
    properties
  }
}
