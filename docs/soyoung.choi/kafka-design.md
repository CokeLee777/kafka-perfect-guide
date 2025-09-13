## 공통
1. node broker 몇 개, 복제본 몇 개

## broker
- broker config
- topic config
1. partition 몇 개


## producer
- producter config. 각 client 에서 설정
### 핵심
- broker 로부터 받는 ack
- ack 가 안 오면 send networkd thread 의 행동을 보기
  - 실제 재전송 하는 주체는 main thread 가 아닌 send thread

1. key 값 유무
   1. 유
   2. **무**
      1. 라운드 로빈
      2. 스티키 파티션
   - 전송 순서의 보장이 필요한가?
2. record 구성
   - topic
   - partition
   - key
   - value
3. 동기 vs **비동기**
- send().get()
  - ack 받아야 다음
- callback
  - ack 기다리지 않고 전송
4. acks = 0,1,**-1(all)**
- producer 가 메시지를 보낸 뒤 broker 에게 ack 확인을 어떻게 받을지 결정하는 옵션. dafault 가 all 임.
    1. 0
       - 동기로 받든, 비동기로 받든 offset=-1 알수없음 의미없는 offset 받음. **cf. partition, topic 은 정상값
       - retry 못함. prodcuer 는 실패 유무를 알 수 없음.
       - 빠름.
    2. 1
    3. -1
       1. min.insync.replicas = ?
          1. 몇 개 보게본 확인하고 ack 줄건지
5. send thread 관련
   - 주의. 동기라면 배치 x. 전송은 배치 레벨이지만 동기라면 당연히 배치에 메시지가 단 한 개 들어갈 걸.
   1. linger.ms = ?
      1. send thread 가 기다리는 최대 대기 시간
        - 0으로 설정해보고, 만약 전송이 느리고, 메시지가 너무 안 갈 땐, 높이기.
        - 보통 20ms 이하로 설정하는 것을 권장
   2. buffer.memory = ?
      1. record accumulator 전체 메모리 사이즈
   3. batch.size = ?
      1. 단일 배치 사이즈
   4. max.inflight.requests.per.connection= ?
      1. broker 서버의 응답없이 acks 없이 producer의 sender thread가 보낼 수 있는 배치의 개수.
      2. defalut 5

6. 재전송
- 주의. ack = 0 이라면 당연히 재전송 매커니즘 쓸 수 없음.
- 재전송을 한다는 것은 데이터가 중복이 될 수도 있다는 것.
  - 메시지를 잘 보냈는데, acks 를 잘 못 보냈을 때.
    1. max.block.ms
       1. send() 호출 시, record accumulator 에 입력하지 못하고 block되는 최대 시간. 초괴 시, timeout exception.
    2. linger.ms
       1. sender thread가 record accumulator에서 배치별로 가져가기 위한 최대 대기 시간
    3. request.timeout.ms
       1. 전송에 걸리는 최대 시간. 전송 재시도 대기 시간 제외. 초과 시, retry 또는 timeout exception 발생
    4. retry.backoff.ms
       1. sender thread가 재시도 하기 전 조금 기다리는 시간. 전송 재시도를 위한 대기 시간.
    5. delivery.tineout.ms producer
       1. 메시지(배치) 전송에 허용된 최대 시간. 초과 시, timeout exception 발생 언제까지 재전송을 시도 하냐. 파라미터 ms 시간 동안 재전송을 시도한다.
       - delivery.timeout.ms >= linger.ms + request.timeout.ms 이여야 한다.
        - 안 그러면 exception
     6. retries
        1. 몇 번 재전송할 거냐. 재전송 횟수를 설정
        2. 굉장히 큰 값을 해둠. 그 횟수 만큼 재전송 시도하다가,
        3. 어차피 delivery.tineout.ms 파라미터에 의해 끝나서 그만큼 안 함.
        4. -> 보통 retires 는 무한대값으로 설정하고
        5. delivery.timeout.ms(기본 120000, 즉 2분) 를 조정하는 것을 권장.
        6. retry 메커니즘을 콘솔로 출력, 확인할 순 없다.

7. 최대 한번 전송, 적어도 한 번 전송, 정확히 한 번 전송
   1. 최대 한번 전송
      1. 재시도 정책 x. 데이터 중복 일어날 일 없음. 소실 될 가능성 up.
   2. 적어도 한번 전송
      1. 중복을 허용. 재전송 정책
   3. 정확히 한번 전송
        - Transaction 기반 전송
          - transaction 
        - Idempotence 멱등성. 보장.
          - producer 레벨에서 message 중복을 제거. 재시도를 하는데, 중복을 제거.
8. default partitioner vs custom partitioner
  



## consumer
- consuemr config. 각 client 에서 설정
### 핵심
- rebalancing

1. consumer 몇 개
   - 파티션 갯수랑 맞추기
2. consumer group
- group.id = ''
- 멀티 구독 모델
  - 한 메시지를 여러 곳(서비스)에서 구독, 소비한다면, 여러 개. 아니면 한 개.