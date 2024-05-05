package com.example.kafka;

import com.github.javafaker.Faker;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class PizzaProducer {

    public static final Logger logger = LoggerFactory.getLogger(PizzaProducer.class.getName());

    public static void sendPizzaMessage(KafkaProducer kafkaProducer, String topicName,
                                        int iterCount, // 몇 번 반복 // -1 무한
                                        int interIntervalMillis, // 1건 보내고 쉬는 시간
                                        int intervalMillis, // 중간 중간 보내고 쉬는 시간 //100ms 쉬기
                                        int intervalCount,// 몇 번 보내고 쉴 지 //100건 보내고
                                        boolean sync) {

        PizzaMessage pizzaMessage = new PizzaMessage();

        long seed = 2022;
        Random random = new Random(seed);
        Faker faker = Faker.instance(random);

        int iterSeq = 0;

        while (iterSeq != iterCount) {
            HashMap<String, String> pMessage = pizzaMessage.produce_msg(faker, random, iterSeq);
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName,
                    pMessage.get("key"), pMessage.get("message"));

            sendMessage(kafkaProducer, producerRecord, pMessage, sync);

            if(intervalCount > 0 && (iterSeq % intervalCount == 0)) {
                try {
                    logger.info("######### IntervalCount " + intervalCount +
                            " intervalMillis : " + intervalMillis + " #######");
                    Thread.sleep(intervalMillis);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage());
                }
            }

            if (interIntervalMillis > 0) {
                try {
                    logger.info(
                            "###### interIntervalMillis : " + interIntervalMillis + " #######");
                    Thread.sleep(interIntervalMillis);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage());
                }
            }

        }

    }

    public static void sendMessage(KafkaProducer<String, String> kafkaProducer,
                                   ProducerRecord<String, String> producerRecord,
                                   HashMap<String, String> pMessage, boolean sync
    ) {
        if (!sync) {
            kafkaProducer.send(producerRecord, (metadata, exception) -> {
                if (exception == null) {
                    logger.info(
                            "async message: " + pMessage.get("key") +
                                    "partition : " + metadata.partition() + "\n" +
                                    "offset : " + metadata.offset() + "\n"
                    );
                } else {
                    logger.error("exception error from broker" + exception.getMessage());
                }
            });
        } else {
            try {
                RecordMetadata recordMetadata = kafkaProducer.send(producerRecord).get();

                logger.info(
                        "sync message: " + pMessage.get("key") +
                                "partition : " + recordMetadata.partition() + "\n" +
                                "offset : " + recordMetadata.offset() + "\n"
                );

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        }
    }


        public static void main (String[]args){

            String topicName = "pizza-topic";

            Properties props = new Properties();
            props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.56.101:9092");
            props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

            KafkaProducer<String, String> kafkaProducer = new KafkaProducer<String, String>(props);

            sendPizzaMessage(kafkaProducer, topicName, -1, 10, 100, 100, true);

            kafkaProducer.close();

        }
    }
