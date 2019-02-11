package com.github.danialk.tutorial2

import java.util.Properties

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.streaming.StreamingMessage
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken, Tweet}
import com.danielasfregola.twitter4s.entities.enums.ResultType
import org.apache.kafka.clients.producer._
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Titter4sTest {
  val consumerToken = ConsumerToken(key = "pIoudfYJj7dUnOpEFldOOJVJr", secret = "EfQCd26CJTp1qslMXrK8j2ONcpC5RdbJoxZKnzEf2QY5AA7Wso")
  val accessToken = AccessToken(key = "41543977-pEM6ACjWgjvpDveNGiAdptCT5O1WOzmqCOJXbxOdS", secret = "ZFed01WcT4o7vkncOnP7XahPC8sV75yVuQDS2XBuAPYM9")

  val restClient = TwitterRestClient(consumerToken, accessToken)
  val streamingClient = TwitterStreamingClient(consumerToken, accessToken)

  val logger = LoggerFactory.getLogger(Titter4sTest.getClass.getName.replace("$", ""))


  def main(args: Array[String]): Unit = {

//    restClient.searchTweet("bitcoin")
    streamingClient.sampleStatuses(stall_warnings = true)(printTweetText)


//    streamingClient.userEvents() {
//      case tweet: Tweet => println(tweet.text)
//    }
//    streamingClient.firehoseStatuses() {
//      case tweet: Tweet => filterTweetByHashtag(tweet, "scala") map printHashtags
//    }

//    val result = searchTweets("#scalax").map { tweets =>
//      println(s"Downloaded ${tweets.size} tweets")
//      tweets.foreach(tweet => {
//        println(tweet.text)
//      })
//    }
  }

  def searchTweets(query: String, max_id: Option[Long] = None): Future[Seq[Tweet]] = {
    def extractNextMaxId(params: Option[String]): Option[Long] = {
      //example: "?max_id=658200158442790911&q=%23scala&include_entities=1&result_type=mixed"
      params.getOrElse("").split("&").find(_.contains("max_id")).map(_.split("=")(1).toLong)
    }

    restClient.searchTweet(query, count = 100, result_type = ResultType.Recent, max_id = max_id).flatMap { ratedData =>
      val result    = ratedData.data
      val nextMaxId = extractNextMaxId(result.search_metadata.next_results)
      val tweets    = result.statuses
      if (tweets.nonEmpty) searchTweets(query, nextMaxId).map(_ ++ tweets)
      else Future(tweets.sortBy(_.created_at))
    } recover { case _ => Seq.empty }
  }

  def batchTweetCallback(tweets: Seq[Tweet]) = {
    tweets.foreach(tweet => {
      println(tweet.text)
    })
  }

  def printTweetText: PartialFunction[StreamingMessage, Unit] = {
    case tweet: Tweet => {
      val producer = createKafkaProducer
      val record = new ProducerRecord[String, String]("twitter_tweets", null, tweet.text)
      // send data
      producer.send(record, new Callback {
        override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
          if (exception != null) {
            logger.error("Error while producing", exception)
          }
        }
      })
    }
  }

  def printHashtags(tweet: Tweet) = tweet.entities.map { e =>
    e.hashtags.foreach { h =>
      println(h.text)
    }
  }

  def filterTweetByHashtag(tweet: Tweet, myAwesomeHashtag: String): Option[Tweet] = tweet.entities.flatMap { e =>
    val hashtagTexts = e.hashtags.map(_.text.toUpperCase)
    if (hashtagTexts.contains(myAwesomeHashtag.toUpperCase)) Some(tweet)
    else None
  }

  def createKafkaProducer: KafkaProducer[String, String] = {
    val config = {
      val properties = new Properties()
      properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092")
      properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
      properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
      properties
    }
    // create the producer
    val producer = new KafkaProducer[String, String](config)
    producer
  }
}
