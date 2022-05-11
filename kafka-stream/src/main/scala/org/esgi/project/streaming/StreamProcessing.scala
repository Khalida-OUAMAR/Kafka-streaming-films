package org.esgi.project.streaming

import io.github.azhur.kafkaserdeplayjson.PlayJsonSupport
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.kstream.{TimeWindows, Windowed}
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream._
import org.esgi.project.streaming.models.{Like, View}
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

  // Declaration of store's names

  val storeMovieName: String = "storeMovieName"

  val viewStartStore: String = "viewStartStore"
  val viewLastMinuteStore: String = "viewLastMinuteStore"
  val viewLast5MinutesStore: String = "viewLast5MinutesStore"

  val movieBestScoreStore: String = "movieBestScoreStore"
  val movieMoreViewStore: String = "movieMoreViewStore"
  val movieWorstScoreStore: String = "movieWorstScoreStore"
  val movieLessViewStore: String = "movieLessViewStore"

  val meanScorePerMovieStore: String = "meanScorePerMovieStore"




  val props = buildProperties

  // defining processing graph
  val builder: StreamsBuilder = new StreamsBuilder

  // TODO: declared topic sources to be used
  val views: KStream[String, View] = builder.stream[String, View](viewsTopicName)
  val likes: KStream[String, Like] = builder.stream[String, Like](likesTopicName)

  /**
   * -----------------
   * Global analyse : Nb views per movie, repartition views per movie
   * -----------------
   */

    val movieName: KGroupedStream[Long, View] = views.groupBy((_, value) => views.title)(Materialized.as(storeMovieName))

    val moviesGroupedByViewCategory: KGroupedStream[Long, View] = views.groupBy((_, value) => views.view_category)

    val viewStart: KTable[Long, Long] = moviesGroupedByViewCategory
       .count()(Materialized.as(viewStartStore))

    val viewLastMinute: KTable[Windowed[Long], Long] = moviesGroupedByViewCategory
      .windowedBy(
        TimeWindows.of(Duration.ofMinutes(1)).advanceBy(Duration.ofSeconds(1))
      )
      .count()(Materialized.as(viewLastMinuteStore))

    val viewLast5Minutes: KTable[Windowed[Long], Long] = moviesGroupedByViewCategory
      .windowedBy(
        TimeWindows.of(Duration.ofMinutes(5)).advanceBy(Duration.ofSeconds(1))
      )
      .count()(Materialized.as(viewLast5MinutesStore))

  /**
   * -------------------
   * Ratings
   * -------------------
   */
  // repartition views per time viewing
  val viewsGroupedByTimeViewing: KGroupedStream[String, View] = ???

  // BestScore, BestView, WorstScore, WorstView
  val movieBestScoreStore: KTable[Windowed[String], Long] = ???

  val movieWorstScoreStore: KTable[Windowed[String], Long] = ???

  val movieMoreViewStore: KTable[Windowed[String], Long] = ???

  val movieLessViewStore: KStream[String, Long] = ???

  val meanScorePerMovieStore: KTable[String, Long] = ???




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
