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
        // key , value : null, "hello world"

        Properties props = new Properties();

        // 3개의 주요 property
        // bootstrap.servers : 브로커를 어디로 보내야 할지
        // key.serializer.class, value.serializer.class : 객체를 serializer 하는

        //props.setProperty("bootstrap.servers", "192.168.56.101:9092");
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.56.101:9092");

        //props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // kafka producer 객체 생성
        // KEY, VALUE (type 맞게, 환경 설정 해준 props 넣어줌)
        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<String, String>(props);

        // ProducerRecord 객체 생성 - message 넣어줌.
        // KEY, VALUE (type 맞게)
        // (topic, key, value) or (topic, value)
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName, "hello word 2");

        // kafkaProducer message send
        // main thread가 send를 호출하여, 이때, producer에서 record 를 serial 하고, partitioner 돌린 후, 내부 buffer에 메시지 저장 후,
        // 별도의 thread 한테 전송해달라고해 실제로 브로커로 전송.
        kafkaProducer.send(producerRecord);

        kafkaProducer.flush();// close 하면 flush 가 되긴 함. // 메시지 하나여서 상관없지만, 배치로 send 수행됨. send된다고 바로 메시지가 가는 게아닌데, 버터에 있던게 바로 나가게 하기 위해
        kafkaProducer.close();
    }
}
