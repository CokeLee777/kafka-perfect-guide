package com.example.kafka;

import java.util.Objects;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

@Slf4j
public class ProducerASyncWithKey {

  public static void main(String[] args) {
    String topicName = "multipart-topic";

    // KafkaProducer configuration setting
    Properties props = new Properties();

    // bootstrap.servers, key.serializer.class value.serializer.class
    props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
    props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.setProperty(
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

    // KafkaProducer object create
    KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(props);

    for (int seq = 0; seq < 20; seq++) {
      // ProducerRecord object create
      ProducerRecord<String, String> producerRecord =
          new ProducerRecord<>(
              topicName, String.valueOf(seq), String.format("hello world %s", seq));

      // KafkaProducer message send
      kafkaProducer.send(
          producerRecord,
          /** 비동기 방식의 callback 이 메서드는 실질적으로 send thread가 호출하게 됨 */
          (metadata, exception) -> {
            if (Objects.isNull(exception)) {
              log.info(
                  "\n ##### record metadata received ##### \n"
                      + "partition:"
                      + metadata.partition()
                      + "\n"
                      + "offset:"
                      + metadata.offset()
                      + "\n"
                      + "timestamp:"
                      + metadata.timestamp());
            } else {
              log.error("exception error from broker " + exception.getMessage());
            }
          });
    }

    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    kafkaProducer.close();
  }
}
