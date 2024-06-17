package com.practice.kafka.producer;

import com.pratice.kafka.event.EventHandler;
import com.pratice.kafka.event.FileEventHandler;
import com.pratice.kafka.event.FileEventSource;
import jdk.jfr.Event;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.File;
import java.util.Properties;

public class FileAppendProducer {

    public static void main(String[] args) {

        String topicName = "file-topic";

        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.56.101:9092");
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<String, String>(props);

        boolean sync = false;
        File file = new File("C:\\study\\kafka-perfect-guide\\src\\soyoung.choi\\KafkaProj-01\\pratice\\src\\main\\resources\\pizza_append.txt");

        EventHandler eventHandler = new FileEventHandler(kafkaProducer, topicName, sync);
        FileEventSource fileEventSource = new FileEventSource(20000, file, eventHandler);
        Thread fileEventSourceThread = new Thread(fileEventSource);
        fileEventSourceThread.start();
    }

}
