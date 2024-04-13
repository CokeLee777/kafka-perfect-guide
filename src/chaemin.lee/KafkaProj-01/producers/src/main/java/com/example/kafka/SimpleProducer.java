package com.example.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class SimpleProducer {

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
    kafkaProducer.send(producerRecord);

    // 버퍼에 담겨 있는 메시지를 즉시 전송한다.
    kafkaProducer.flush();
    kafkaProducer.close();
  }
}
