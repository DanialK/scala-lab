package com.github.danialk.tutorial1

import java.time.Duration
import java.util.Properties

import org.apache.kafka.clients.consumer.{KafkaConsumer, _}
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

object ConsumerDemoGroup {
  def main(args: Array[String]): Unit = {
    val logger = LoggerFactory.getLogger(ConsumerDemoWithThreads.getClass.getName.replace("$", ""))

    // create producer properties
    val config = {
      val properties = new Properties()
      properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092")
      properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
      properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
      properties.put(ConsumerConfig.GROUP_ID_CONFIG, "my-test-group22")
      properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      properties
    }
    // create the consumer
    val consumer = new KafkaConsumer[String, String](config)

    // subscribe consumer ot our topic
    consumer.subscribe(List("first_topic").asJava);

    // poll for new data
    while (true) {
      val records: ConsumerRecords[String, String] = consumer.poll(Duration.ofMillis(100))
      records.asScala.foreach(record => {
        logger.info("Key: " + record.key() + ", Value: " + record.value())
        logger.info("Partition: " + record.partition() + ", Offset: " + record.offset())
      })
    }
  }
}
