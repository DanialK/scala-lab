package com.github.danialk.tutorial1

import java.time.Duration
import java.util.Properties
import java.util.concurrent.CountDownLatch

import org.apache.kafka.clients.consumer.{KafkaConsumer, _}
import org.apache.kafka.common.errors.WakeupException
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

object ConsumerDemoWithThreads {
  class ConsumerRunnable(topic: String,
                         bootstrapServers: String,
                         groupId: String,
                         private val latch: CountDownLatch) extends Runnable {

    private val logger: Logger = LoggerFactory.getLogger(this.getClass.getName.replace("$", ""))

    val config: Properties = {
      val properties = new Properties()
      properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
      properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
      properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
      properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
      properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      properties
    }

    val consumer = new KafkaConsumer[String, String](config)

    // subscribe consumer ot our topic
    consumer.subscribe(List(topic).asJava)

    override def run(): Unit = {
      try {
        // poll for new data
        while (true) {
          val records: ConsumerRecords[String, String] = consumer.poll(Duration.ofMillis(100))
          records.asScala.foreach(record => {
            logger.info("Key: " + record.key() + ", Value: " + record.value())
            logger.info("Partition: " + record.partition() + ", Offset: " + record.offset())
          })
        }
      } catch {
        case _: WakeupException =>
          logger.info("Received shutdown signal!")
      } finally {
        consumer.close()
        // tell our main code we're done with the consumer
        latch.countDown()
      }
    }

    def shutdown(): Unit = {
      // wakeup is a special method to interrupt consumer.poll()
      // it will throw the exception WakeUpException
      consumer.wakeup()
    }
  }

  def main(args: Array[String]): Unit = {
    val logger = LoggerFactory.getLogger(ConsumerDemoWithThreads.getClass.getName.replace("$", ""))

    val latch = new CountDownLatch(1)
    logger.info("Creating the consumer thread")
    val myConsumerRunnable = new ConsumerRunnable(
      "first_topic",
      "127.0.0.1:9092",
      "my-test-group22",
      latch
    )

    val myThread = new Thread(myConsumerRunnable)

    myThread.start()

    val t = new Thread {
      override def run = {
        logger.info("Caught shutdown hook")
        myConsumerRunnable.shutdown()
        latch.await()
        logger.info("Application has exited")

      }
    }

    sys.addShutdownHook(t.run)

    try {
      latch.await()
    } catch {
      case e: InterruptedException => logger.error("Application got interrupted", e)
    } finally  {
      logger.info("Application is closing")
    }
  }
}