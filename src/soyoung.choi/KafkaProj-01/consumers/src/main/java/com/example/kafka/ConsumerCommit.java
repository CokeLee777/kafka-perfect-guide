package com.example.kafka;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ConsumerCommit {

    public static final Logger logger = LoggerFactory.getLogger(ConsumerCommit.class.getName());

    public static void main(String[] args) {

        Properties props = new Properties();

        String topicName = "pizza-topic";

        // 필수 consumer config
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.56.101:9092");
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "group_03");

//        props.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "6000");

        props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<String, String>(props);
        kafkaConsumer.subscribe(List.of(topicName));

        // main thread
        Thread mainThread = Thread.currentThread();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            // 해야 할 로직
            public void run() {
                logger.info("main program starts to exit by calling wakeup");

                //kafkaConsumer.wakeup();

                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        //pollAutoCommit(kafkaConsumer);

        //pollCommitSync(kafkaConsumer);

        pollCommitAsync(kafkaConsumer);
    }

    private static void pollCommitAsync(KafkaConsumer<String, String> kafkaConsumer) {
        int loopCnt = 0;

        try {
            while (true) {
                ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(1000)); // 배치 단위 메시지 // 1초 동안 기다리게
                logger.info(" ###### loopCnt :{} consumerRecords count : {}", loopCnt++, consumerRecords.count());
                for(ConsumerRecord  record: consumerRecords) {
                    logger.info("record key : {}, partition : {}, record offset : {}, record value : {}"
                            , record.key(), record.partition(), record.offset(), record.value());
                }

                kafkaConsumer.commitAsync(new OffsetCommitCallback() {
                    @Override
                    public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
                        if (exception != null) {
                            logger.error("offsets {} is not completed, error : {}", offsets, exception);
                        }
                    }
                });

            }
        }catch (WakeupException e) {
            logger.error("wakeup exception hsa been called");

        }catch (Exception e) {
            logger.error(e.getMessage());
        }

        finally {
            logger.error("finally consumer is closing");
            kafkaConsumer.close();
        }

    }

    private static void pollCommitSync(KafkaConsumer<String, String> kafkaConsumer) {

        int loopCnt = 0;

        try {
            while (true) {
                ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(1000)); // 배치 단위 메시지 // 1초 동안 기다리게
                logger.info(" ###### loopCnt :{} consumerRecords count : {}", loopCnt++, consumerRecords.count());
                for(ConsumerRecord  record: consumerRecords) {
                    logger.info("record key : {}, partition : {}, record offset : {}, record value : {}"
                            , record.key(), record.partition(), record.offset(), record.value());
                }
                try {
                    if (consumerRecords.count() > 0) {
                        kafkaConsumer.commitSync();
                        logger.info("commit sync has been called");
                    }
                }catch (CommitFailedException e){
                    logger.error(e.getMessage());
                }
            }
        }catch (WakeupException e) {
            logger.error("wakeup exception hsa been called");

        }catch (Exception e) {
            logger.error(e.getMessage());
        }

        finally {
            kafkaConsumer.commitSync();
            logger.error("finally consumer is closing");
            kafkaConsumer.close();
        }
    }

    private static void pollAutoCommit(KafkaConsumer<String, String> kafkaConsumer) {

        int loopCnt = 0;

        try {
            while (true) {
                ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(1000)); // 배치 단위 메시지 // 1초 동안 기다리게
                logger.info(" ###### loopCnt :{} consumerRecords count : {}", loopCnt++, consumerRecords.count());
                for(ConsumerRecord  record: consumerRecords) {
                    logger.info("record key : {}, partition : {}, record offset : {}, record value : {}"
                            , record.key(), record.partition(), record.offset(), record.value());
                }
                try {
                    logger.info("main thread is sleeping {} ms during while loop", 10000);
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
