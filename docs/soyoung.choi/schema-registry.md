
# schema registry

https://docs.confluent.io/platform/current/schema-registry/index.html

- 운영에선 요구사항이 변경될 때, 메시지의 형태가 변하는 때가 있을 것. 이때, prodcuer 에서 메시지 형태가 변경된 채 보냈는데, 이를 consumer가 대응하지 못한다면 에러가 날 것. 이를 방지하기 게 schema registry
-  RESTful 인터페이스를 사용하여 스키마(Schema)를 관리하거나 조회하는 기능을 제공

### 장점
-  메시지의 스키마를 보다 안전하게 관리할 수 있음.
-  프로듀서와 컨슈머가 직접 스키마를 관리할 필요가 없음.
-  잘못된 스키마를 가진 메시지를 전달한다면, schema registry 에 등록되는 과정에서 실패가 되고 카프카에 메시지가 전달되지 않는다.
-  카프카에 전달도는 바이너리 데이터에 스키마 정보가 포함되어 있지 않기에, 상대적으로 적은 용량ㅇ의 데이터가 전달된다는 점.  JSON 등과 비교하여 Kafka 시스템에서 더 적은 공간만 차지하게 되고 네트워크 대역폭도 절약
  
### 단점
- Schema Registry 의 역할이 굉장히 중요하고 Schema Registry 의 장애가 발생하는 경우 정상적으로 메시지를 전달하지 못하게 됨. 운영 포인트 증가.
- Avro 포맷은 JSON등과 비교하여 직렬화과정에서 퍼포먼스가 조금 떨어진다고 함.

### avro 포맷 형태
- producer 는 avro 형태로 broker 에 보내는데,
- schema 는 schema registry로, binary data 는 broker 로 간다.
1. schema
  - 데이터 형태
2. binary data
  - 실 데이터

### 프로세스
1. schema registry 에 schema 등록
2. producer 는 Producer는 등록된 스키마를 읽어온다. 메시지를 생성할 때, 직렬화를 수행한 뒤 메시지를 생성한다. 만약, producr가 등록된 스키마가 아닌 메시지를 보내려고 한다면, ValueError 가 발생한다.
3. consumer 는 broker 로부터 binary data 를 받는다. 이때, 이 데이터에 schema id 를 포함 시킨다.
   - 해당 id 정보를 통해 로컬캐시 혹은 Confluent Schema Registry 에서 스키마정보를 탐색하여 가져오고, 스키마정보를 사용하여  avro deserializer 로 역직렬화하여 사용하게 됨.

## 설계
### 스키마 진화 전략
1. Backward : 새로운 스키마로 이전 데이터를 읽는것이 가능한것을 의미
- 새로운 스키마에 필드가 추가되었는데 해당 필드에 default value 가 없다면 오류가 발생할 것이므로 스키마 등록을 허용하지 않게 된다.
- 새로 추가되는 필드에 default value 를 지정할때에만 스키마 등록이 허용이 된다.
- Confluent Schema Registry 는 기본적으로 Backward 로 동작(물론 설정에서 변경할 수 있습니다.)
2. Forward : 이전 스키마에서 새로운 데이터를 읽는것이 가능한것을 의미
- 새로운 스키마에서 특정 필드가 삭제된다면, 해당 필드는 이전 스키마에서 default value 를 가지고 있었어야 함.
3. Full : Backward 와 Forward 을 모두 만족함하는 것. 가능하다면 이것을 사용하기를 권장.
4. None : Backward 와 Forward 어느 한쪽 만족하지 못함. 아주 예외적인 상황을 제외한다면 사용하지 않기를 권장.

- 만약 항상 모든 Consumer 가 먼저 배포되고 이후에 Producer 가 배포된다면 Backward 로 충분
- 하지만 그것을 확신할 수 없다면 Full 을 사용하는것을 권장

### 고려 사항
- 삭제될 가능성이 있는 필드라면 default value 를 지정하자. 해당 필드에 데이터가 들어오지 않더라도 에러가 발생하지 않음.
- 추가되는 필드라면 default value 가 지정되어야 함.
- Enum 은 변경될 가능성이 없을때에만 사용하도록 함.
- 필드의 이름은 변경하지 않도록 함.

### 단독 설치
https://taes-k.github.io/2021/02/26/schema-registry-only/

### 실행
~~~
# Schema Registry 실행
$ ./bin/schema-registry-start ./etc/schema-registry/schema-registry.properties
# Schema Registry 에 등록된 스키마 조회 (기본포트 8081)
$ curl -X GET http://localhost:8081/subjects
[] 
# compatibilityLevel 설정
$ curl -X GET http://localhost:8081/config
{"compatibilityLevel":"BACKWARD"} # 기본적으로 BACKWARD 를 사용하고 있습니다.
$ curl --header "Content-Type: application/json" -X PUT   --data '{"compatibility": "FULL"}'   http://localhost:8081/config
{"compatibility":"FULL"} # FULL 로 변경되었습니다.
~~~

### topic 에 스키마 등록
- **api 를 통한, avro schema 타입 등록하는 방법**

https://medium.com/@tlsrid1119/kafka-schema-registry-feat-confluent-example-cde8a276f76c

~~~
# API를 통한 Schema 등록
METHOD: POST 
URI: http://localhost:8081/subjects/user-topic-value/versions
body: {"schema": "{\"type\":\"record\",\"name\":\"User\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"age\",\"type\":\"int\"}]}"}

# 확인
METHOD: GET
URI: http://localhost:8081/subjects/user-topic-value/versions

# response
{
    "subject": "user-topic-value",
    "version": 1,
    "id": 3,
    "schema": "{\"type\":\"record\",\"name\":\"User\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"age\",\"type\":\"int\"}]}"
}
~~~

### producer
~~~
	from confluent_kafka import Producer
from confluent_kafka.schema_registry import SchemaRegistryClient
from confluent_kafka.schema_registry.avro import AvroSerializer
from confluent_kafka.serialization import MessageField, SerializationContext
 
schema_registry_client = SchemaRegistryClient({"url": "http://localhost:8081"})
schema_str = schema_registry_client.get_schema(schema_id=1).schema_str
avro_serializer = AvroSerializer(schema_registry_client, schema_str)
 
producer = Producer({"bootstrap.servers": "localhost:9092"})
 
topic_name = "users"
message = {"userid": 4, "gender": "FEMALE", "username": "광수"}
 
producer.produce(
    topic=topic_name,
    value=avro_serializer(
        message, SerializationContext(topic_name, MessageField.VALUE)
    ),
)
producer.flush()
~~~

### consumer 
~~~
from confluent_kafka.schema_registry import SchemaRegistryClient
from confluent_kafka.schema_registry.avro import AvroDeserializer
from confluent_kafka.serialization import MessageField, SerializationContext
 
schema_registry_client = SchemaRegistryClient({"url": "http://localhost:8081"})
schema_str = schema_registry_client.get_schema(schema_id=1).schema_str
avro_deserializer = AvroDeserializer(schema_registry_client, schema_str)
 
# 역직렬화
user = avro_deserializer(message.value(), SerializationContext(message.topic(), MessageField.VALUE))
user
~~~
https://suwani.tistory.com/155