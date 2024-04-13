package com.example.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Objects;

@Slf4j
public class CustomerCallback implements Callback {

  private final int seq;

  public CustomerCallback(int seq) {
    this.seq = seq;
  }

  @Override
  public void onCompletion(RecordMetadata metadata, Exception exception) {
    if (Objects.isNull(exception)) {
      log.info("seq:{} partition:{} offset:{}", this.seq, metadata.partition(), metadata.offset());
    } else {
      log.error("exception error from broker " + exception.getMessage());
    }
  }
}
