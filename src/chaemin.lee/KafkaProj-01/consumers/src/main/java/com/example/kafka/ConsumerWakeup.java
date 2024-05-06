package com.example.kafka;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;

@Slf4j
public class ConsumerWakeup {

    public static void main(String[] args){
        String topicName = "simple-topic";

        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "group_01");
//        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(props);
        kafkaConsumer.subscribe(List.of(topicName));

        // main thread 참조 변수
        Thread mainThread = Thread.currentThread();

        // main thread 종료 시 별도의 thread로 kafka consumer wakeup() 메서드 호출
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("main program starts to exit by calling wakeup");
            // KafkaConsumer가 poll 수행중에 wakeup exception을 발생시킴
            kafkaConsumer.wakeup();

            try {
                mainThread.join();
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }));

        try {
            while(true) {
                ConsumerRecords<String, String> consumerRecords
                        = kafkaConsumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : consumerRecords) {
                    log.info("record key:{}, record value:{}, partition:{}, record offset:{}",
                            record.key(), record.value(), record.partition(), record.offset());
                }
            }
        } catch(WakeupException e) {
            log.error("wakeup exception has been called");
        } finally {
            log.info("finally consumer is closing");
            kafkaConsumer.close();
        }
    }
}
