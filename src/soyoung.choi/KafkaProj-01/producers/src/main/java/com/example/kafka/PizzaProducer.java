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

            // acks setting
            //props.setProperty(ProducerConfig.ACKS_CONFIG, "0");

            // batch setting
            //props.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, "32000");
            //props.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20");

            // retry setting
            //props.setProperty(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, "50000");

            // idempotence setting
            // 1. 명시적 설정 안 하고, 디폴트 상황이라면,
            props.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "6");
            // 5 이상이므로 멱등성으로 안 보내짐. // 콘솔로 확인할 방법은 없음.
            props.setProperty(ProducerConfig.ACKS_CONFIG, "0");
            // producer 가지만, 멱등석으로 안 보내짐.

            // 2. 원래 디폴트 상황이지만, 아래와 같이 명시적으로 설정했다면,
            // 이때, 위와 같이 맞지 않는 파라미터 설정을 했다면,  config 오류가 발생하면서 producer 가 기동되지 않음.
            props.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");


            KafkaProducer<String, String> kafkaProducer = new KafkaProducer<String, String>(props);
            sendPizzaMessage(kafkaProducer, topicName, -1, 10, 100, 100, true);

            kafkaProducer.close();

        }
    }
