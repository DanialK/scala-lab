package com.github.danialk.tutorial1
import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, _}
import org.apache.kafka.common.serialization.StringSerializer

object ProducerDemo {
  def main(args: Array[String]): Unit = {
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

    val record = new ProducerRecord[String, String]("first_topic", "hello world 11/11/18")
    // send data
    producer.send(record)

    // flush data
    producer.flush()

    // flush and close
    producer.close()
  }
}
