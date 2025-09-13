
### confluent kafka install
~~~
pip install confluent-kafka
~~~

## consumer
1. config
- confluent-kafka-python 라이브러리에서 컨슈머 설정은 파이썬 딕셔너리를 사용하여 정의. 

```
from confluent_kafka import Consumer

config = {
    'bootstrap.servers': 'localhost:9092',  # Kafka 브로커 주소
    'group.id': 'my_consumer_group',        # 컨슈머 그룹 ID
    'auto.offset.reset': 'earliest',        # 메시지 소비 시작 위치 (earliest: 가장 처음, latest: 가장 마지막)
    'enable.auto.commit': True,             # 자동 커밋 활성화
    'auto.commit.interval.ms': 5000         # 자동 커밋 간격 (ms)
}



consumer = Consumer(config)
```

2. config _ 설정파일로 관리
- JSON 또는 YAML 파일로 작성한 후, 파이썬 스크립트에서 해당 파일을 읽어와 딕셔너리 형태로 변환하여 KafkaConsumer 객체를 생성할 때 전달하
- 분리해서, local, dev, prod 분리하기
- 설정파일 yaml
~~~
    # config.yaml
    bootstrap.servers: "localhost:9092"
    group.id: "my_group"
    auto.offset.reset: "earliest"
~~~

- 설정 파일 들고오기
~~~
    import yaml
    # import json # YAML 대신 JSON 파일을 사용하는 경우

    def load_config(file_path):
        with open(file_path, 'r') as f:
            # YAML 사용 시
            config = yaml.safe_load(f)
            # JSON 사용 시
            # config = json.load(f)
        return config
~~~

- 컨슈머 설정
~~~
    from confluent_kafka import KafkaConsumer

    # 로드할 설정 파일 경로 지정
    config_file_path = 'config.yaml' # 또는 'config.json'
    
    # 설정 파일 로드
    consumer_config = load_config(config_file_path)

    # KafkaConsumer 객체 생성 시 설정 적용
    consumer = KafkaConsumer(
        'your_topic_name', # 구독할 토픽 이름
        **consumer_config # 딕셔너리 형태로 전달
    )

    # 메시지 소비 로직
    for msg in consumer:
        print(f"Received message: {msg.value().decode('utf-8')}")

    consumer.close()
~~~