package com.github.danialk.tutorial1

import java.time.Duration
import java.util.Properties

import org.apache.kafka.clients.consumer.{KafkaConsumer, _}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import scala.util.control.Breaks._
import scala.collection.JavaConverters._

object ConsumerDemoAssignSeek {
  def main(args: Array[String]): Unit = {
    val logger = LoggerFactory.getLogger(ConsumerDemoWithThreads.getClass.getName.replace("$", ""))

    // create producer properties
    val config = {
      val properties = new Properties()
      properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092")
      properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
      properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
      properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      properties
    }
    // create the consumer
    val consumer = new KafkaConsumer[String, String](config)

    // Assign and Seek are mostly used to reply data or fetch a specific message
    // For replay use cases

    // Assign
    val partitionToReadFrom = new TopicPartition("testLogs",0)
    val offsetToReadFrom = 15L
    consumer.assign(List(partitionToReadFrom).asJava)

    // Seek
    consumer.seek(partitionToReadFrom, offsetToReadFrom)

    val numberOfMessagesToRead = 5
    var keepOnReading = true
    var numberOfMessagesReadSoFar = 0

    // poll for new data
    while (keepOnReading) {
      val records: ConsumerRecords[String, String] = consumer.poll(Duration.ofMillis(100))
      breakable {
        for (record <- records.asScala) {
          numberOfMessagesReadSoFar += 1
          logger.info("Key: " + record.key() + ", Value: " + record.value())
          logger.info("Partition: " + record.partition() + ", Offset: " + record.offset())
          if (numberOfMessagesReadSoFar >= numberOfMessagesToRead) {
            keepOnReading = false
            break
          }
        }
      }
    }
  }
}
