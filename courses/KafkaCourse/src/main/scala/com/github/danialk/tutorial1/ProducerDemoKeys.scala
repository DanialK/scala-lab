package com.github.danialk.tutorial1

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, _}
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory

object ProducerDemoKeys {
  def main(args: Array[String]): Unit = {
    val logger = LoggerFactory.getLogger(ProducerDemoKeys.getClass.getName.replace("$", ""))

    // create producer properties
    val config = {
      val properties = new Properties()
      properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092")
      properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
      properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
      properties
    }
    // create the producer
    val producer = new KafkaProducer[String, String](config)

    (1 to 10).foreach ((i: Int) => {
      val topic = "first_topic"
      val value = s"hello world 11/11/18 $i"
      val key = s"id_$i"
      // by providing a key, we guarantee the same key goes to the same partition, for a fixed number of partitions

      val record = new ProducerRecord[String, String](topic, key, value)
      logger.info("Key: " + key);
      // send data
      producer.send(record, new Callback {
        override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
          if (exception == null) {
            logger.info("Received new metadata. \n" +
              "Topic: " + metadata.topic() + "\n" +
              "Partition: " + metadata.partition() + "\n" +
              "Offset: " + metadata.offset() + "\n" +
              "Timestamp: " + metadata.timestamp())
          } else {
            logger.error("Error while producing", exception)
          }
        }
      }).get() // blocks the .send to make it synchronous - don't do this in production
    })


    // flush data
    producer.flush()

    // flush and close
    producer.close()
  }
}
