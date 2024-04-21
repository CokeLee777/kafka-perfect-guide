package com.example.kafka;

import java.util.HashMap;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

@Slf4j
public class PizzaProducer {

  public static void sendPizzaMessage(
      KafkaProducer<String, String> kafkaProducer,
      String topicName,
      int iterCount,
      int interIntervalMillis,
      int intervalMillis,
      int intervalCount,
      boolean sync) {
    PizzaMessage pizzaMessage = new PizzaMessage();
    int iterSeq = 0;
    long seed = 2022;
    Random random = new Random(seed);
    Faker faker = Faker.instance(random);

    while (iterSeq++ != iterCount) {
      HashMap<String, String> pMessage = pizzaMessage.produce_msg(faker, random, iterSeq);
      ProducerRecord<String, String> producerRecord =
          new ProducerRecord<>(topicName, pMessage.get("key"), pMessage.get("message"));
      sendMessage(kafkaProducer, producerRecord, pMessage, sync);

      if ((intervalCount > 0) && (iterSeq % intervalCount == 0)) {
        try {
          log.info("##### IntervalCount:{} intervalMillis:{} #####", intervalCount, intervalMillis);
          Thread.sleep(intervalMillis);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

      if (interIntervalMillis > 0) {
        try {
          log.info("interIntervalMillis:{}", interIntervalMillis);
          Thread.sleep(interIntervalMillis);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static void sendMessage(
      KafkaProducer<String, String> kafkaProducer,
      ProducerRecord<String, String> producerRecord,
      HashMap<String, String> pMessage,
      boolean sync) {
    if (!sync) {
      kafkaProducer.send(
          producerRecord,
          /** 비동기 방식의 callback 이 메서드는 실질적으로 send thread가 호출하게 됨 */
          (metadata, exception) -> {
            if (Objects.isNull(exception)) {
              log.info(
                  "async message:"
                      + pMessage.get("key")
                      + "partition:"
                      + metadata.partition()
                      + "\n"
                      + "offset:"
                      + metadata.offset());
            } else {
              log.error("exception error from broker " + exception.getMessage());
            }
          });
    } else {
      try {
        RecordMetadata metadata = kafkaProducer.send(producerRecord).get();
        log.info(
            "sync message:"
                + pMessage.get("key")
                + "partition:"
                + metadata.partition()
                + "\n"
                + "offset:"
                + metadata.offset());
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (ExecutionException e) {
        e.printStackTrace();
      }
    }
  }

  public static void main(String[] args) {
    String topicName = "pizza-topic";

    // KafkaProducer configuration setting
    Properties props = new Properties();

    // bootstrap.servers, key.serializer.class value.serializer.class
    props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
    props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.setProperty(
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

    // acks setting
//    props.setProperty(ProducerConfig.ACKS_CONFIG, "0");

    // batch setting
    props.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, "32000");
    props.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20");

    // KafkaProducer object create
    KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(props);

    sendPizzaMessage(kafkaProducer, topicName, -1, 10, 100, 100, false);

    kafkaProducer.close();
  }
}
