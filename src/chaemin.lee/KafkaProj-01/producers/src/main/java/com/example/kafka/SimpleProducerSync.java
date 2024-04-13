package com.example.kafka;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class SimpleProducerSync {

  public static void main(String[] args) {
    String topicName = "simple-topic";

    // KafkaProducer configuration setting
    Properties props = new Properties();

    // bootstrap.servers, key.serializer.class value.serializer.class
    props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
    props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.setProperty(
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

    // KafkaProducer object create
    KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(props);

    // ProducerRecord object create
    ProducerRecord<String, String> producerRecord =
        new ProducerRecord<>(topicName, "hello world 2");

    // KafkaProducer message send
    // send 한다고 바로 메시지가 전송되지 않는다.
    try {
      RecordMetadata recordMetadata = kafkaProducer.send(producerRecord).get();
      log.info("\n ##### record metadata received ##### \n" +
              "partition:" + recordMetadata.partition() + "\n" +
              "offset:" + recordMetadata.offset() + "\n" +
              "timestamp:" + recordMetadata.timestamp());
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    } finally {
      kafkaProducer.close();
    }
  }
}
