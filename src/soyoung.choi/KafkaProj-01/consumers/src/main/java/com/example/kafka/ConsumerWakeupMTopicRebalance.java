package com.example.kafka;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class ConsumerWakeupMTopicRebalance {

    public static final Logger logger = LoggerFactory.getLogger(ConsumerWakeupMTopicRebalance.class.getName());

    public static void main(String[] args) {

        Properties props = new Properties();

        //String topicName = "pizza-topic";

        // 필수 consumer config
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.56.101:9092");
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "group-assign");

        // Consumer 파티션 할당 전략
//        props.setProperty(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, RoundRobinAssignor.class.getName());
        props.setProperty(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, CooperativeStickyAssignor.class.getName());

        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<String, String>(props);
        kafkaConsumer.subscribe(List.of("topic-p3-t1", "topic-p3-t2"));

        // main thread
        Thread mainThread = Thread.currentThread();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            // 해야 할 로직
            public void run() {
                logger.info("main program starts to exit by calling wakeup");

                kafkaConsumer.wakeup(); // this.client.wakeup(); => ConsumerClientNetwork

                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            while (true) {
                ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(1000)); // 배치 단위 메시지 // 1초 동안 기다리게

                for(ConsumerRecord  record: consumerRecords) {
                    logger.info(" topic : {}, record key : {}, partition : {}, record offset : {}, record value : {}"
                            , record.topic(), record.key(), record.partition(), record.offset(), record.value());
                }
            }
        }catch (WakeupException e) {
            logger.error("wakeup exception hsa been called");

        } finally {
            logger.error("finally consumer is closing");
            kafkaConsumer.close();
        }

    }
}
