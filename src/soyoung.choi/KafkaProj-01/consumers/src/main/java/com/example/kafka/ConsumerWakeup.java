package com.example.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class ConsumerWakeup {

    public static final Logger logger = LoggerFactory.getLogger(ConsumerWakeup.class.getName());

    public static void main(String[] args) {

        Properties props = new Properties();

        String topicName = "simple-topic";

        // 필수 consumer config
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.56.101:9092");
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "group_01");

        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<String, String>(props);
        kafkaConsumer.subscribe(List.of(topicName));

        // main thread
        Thread mainThread = Thread.currentThread();

        // shutdown 할 때, hook 걸어줌.
        // 인자로 넣어, 새로운 임시 Thread를 생성해,
        // 죽기 전에, main thread 가 해야 할 것을 수행 해준다.
        // main thread 종료 시 별도의 thread 로 KafkaConsumer wakeup()메소드를 호출하게 함.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            // 해야 할 로직
            public void run() {
                logger.info("main program starts to exit by calling wakeup");

                // poll 수행 중 wakeupException 을 발생시키는 메소드
                // main thread 프로그램이 중료할 때, exception 을 터트려야 한다.
                kafkaConsumer.wakeup(); // this.client.wakeup(); => ConsumerClientNetwork

                //main thread가 죽을때까지 만들어준 임시 thread 가 기다려서 같이 죽어야 하므로,
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
                    logger.info("record key : {}, record value : {}, partition : {}, record offset : {}"
                            , record.key(), record.value(), record.partition(), record.offset());
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
