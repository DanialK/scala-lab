package com.github.danialk.tutorial2

import java.util.Properties

import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.streaming.StreamingMessage
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken, Tweet}
import org.apache.kafka.clients.producer._
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.{Logger, LoggerFactory}

object Twitter4sTest {
  val consumerToken = ConsumerToken(key = "pIoudfYJj7dUnOpEFldOOJVJr", secret = "EfQCd26CJTp1qslMXrK8j2ONcpC5RdbJoxZKnzEf2QY5AA7Wso")
  val accessToken = AccessToken(key = "41543977-pEM6ACjWgjvpDveNGiAdptCT5O1WOzmqCOJXbxOdS", secret = "ZFed01WcT4o7vkncOnP7XahPC8sV75yVuQDS2XBuAPYM9")

  val streamingClient = TwitterStreamingClient(consumerToken, accessToken)

  val logger: Logger = LoggerFactory.getLogger(Twitter4sTest.getClass.getName.replace("$", ""))

  def main(args: Array[String]): Unit = {

    val producer = createKafkaProducer

    streamingClient.sampleStatuses(stall_warnings = true)(printTweetText(producer))

    val t = new Thread {
      override def run: Unit = {
        logger.info("Caught shutdown hook")
        streamingClient.shutdown()
        producer.close()
        logger.info("Application has exited")
      }
    }

    sys.addShutdownHook(t.run)
  }

  def printTweetText(producer: KafkaProducer[String, String]): PartialFunction[StreamingMessage, Unit] = {
    case tweet: Tweet => {
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
