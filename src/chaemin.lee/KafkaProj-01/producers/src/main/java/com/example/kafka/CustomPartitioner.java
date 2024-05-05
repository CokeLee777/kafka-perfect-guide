package com.example.kafka;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.clients.producer.internals.StickyPartitionCache;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.InvalidRecordException;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.utils.Utils;

@Slf4j
public class CustomPartitioner implements Partitioner {

    private final StickyPartitionCache stickyPartitionCache = new StickyPartitionCache();
    private String specialKeyName;

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        // 해당 토픽의 파티션 정보들을 가져옴
        List<PartitionInfo> partitionInfos = cluster.partitionsForTopic(topic);
        int numPartitions = partitionInfos.size();
        int numSpecialPartitions = (int)(numPartitions * 0.5);
        int partitionIndex = 0;

        // 키가 없다면 에러 발생
        if(keyBytes == null) {
            throw new InvalidRecordException("key should not be null");
        }

        if(((String)key).equals(specialKeyName)) {
            partitionIndex = Utils.toPositive(Utils.murmur2(valueBytes)) % numSpecialPartitions;
        } else {
            partitionIndex = Utils.toPositive(Utils.murmur2(keyBytes)) % (numPartitions - numSpecialPartitions) + numSpecialPartitions;
        }
        log.info("key:{} is sent to partition:{}", key.toString(), partitionIndex);

        return partitionIndex;
    }

    @Override
    public void close() {

    }

    // 우리가 설정한 카프카 설정정보가 인자로 넘어옴
    @Override
    public void configure(Map<String, ?> configs) {
        specialKeyName = configs.get("custom.specialKey").toString();
    }
}
