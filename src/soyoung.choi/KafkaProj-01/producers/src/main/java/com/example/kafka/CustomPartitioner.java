package com.example.kafka;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.clients.producer.internals.DefaultPartitioner;
import org.apache.kafka.clients.producer.internals.StickyPartitionCache;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.InvalidRecordException;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class CustomPartitioner implements Partitioner {

    public static final Logger logger = LoggerFactory.getLogger(CustomPartitioner.class.getName());
    private final StickyPartitionCache stickyPartitionCache = new StickyPartitionCache();

    private String specialKeyName;

    @Override
    public void configure(Map<String, ?> configs) {
        specialKeyName = configs.get("custom.specialKey").toString();

    }

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        // byte[] keyBytes -> 직렬화된 것.
        // cluster : 브로커의 토픽 정보를 해싱해서 가지고 있음.

        List<PartitionInfo>  partitionInfoList = cluster.partitionsForTopic(topic); //5개 일때, 5%2
        int numPartitions = partitionInfoList.size();
        int numSpecialPartitions = (int) (numPartitions * 0.5);

        int partitionIndex = 0;

        if (keyBytes == null) {
            return stickyPartitionCache.partition(topic, cluster);

            // 이 경우, 예외를 던지고 싶으면,
//            throw new InvalidRecordException("key should not be bull");
        }

        if( ((String) key).equals(specialKeyName) ){
            partitionIndex = Utils.toPositive(Utils.murmur2(valueBytes)) % numSpecialPartitions; // 0, 1 나오게
        } else {
            partitionIndex = Utils.toPositive(Utils.murmur2(keyBytes)) % (numPartitions - numSpecialPartitions) + numSpecialPartitions; // 2,3,4 나오게
        }
        logger.info("key:{} is sent to partition: {}", key.toString(), partitionIndex);


        return partitionIndex;
    }

    @Override
    public void close() {

    }

}
