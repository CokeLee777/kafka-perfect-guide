package com.example.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class SimpleConsumer {

    public static final Logger logger = LoggerFactory.getLogger(SimpleConsumer.class.getName());

    public static void main(String[] args) {

        Properties props = new Properties();

        String topicName = "simple-topic";

        // 필수 consumer config
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.56.101:9092");
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "simple-group");

        // HeartBeat
        props.setProperty(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "5000");
        props.setProperty(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "90000");
        props.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "60000");

        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<String, String>(props);

        kafkaConsumer.subscribe(List.of(topicName));

        while (true) {
            ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(1000)); // 배치 단위 메시지 // 1초 동안 기다리게

            for(ConsumerRecord  record: consumerRecords) {
                logger.info("record key : {}, record value : {}, partition : {}"
                        , record.key(), record.value(), record.partition());
            }
        }
        // close 를 명시적으로 해줘야 함. 하면 meta 정보까지 정리됨.
        // kafkaConsumer.close();
    }
}
