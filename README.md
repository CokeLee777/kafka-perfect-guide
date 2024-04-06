# kafka-perfect-guide
This repository is about kafka perfect guide

## docs

- [Soyoung Choi Docs](https://github.com/CokeLee777/kafka-perfect-guide/tree/main/docs/soyoung.choi)
- [Chaemin Lee Docs](https://github.com/CokeLee777/kafka-perfect-guide/tree/main/docs/chaemin.lee)

## Confluent Kafka Setting Guide [Mac OS]

1. Install Java 11 and set environment variable

```shell
# JAVA 11 Path
export JAVA_HOME=$(/usr/libexec/java_home -v 11)
export PATH=${PATH}:$JAVA_HOME/bin
```

```shell
source .zshrc
```

2. Install Confluent Kafka

[Confluent Kafka homepage](https://docs.confluent.io/platform/current/installation/installing_cp/zip-tar.html)

2-1. Edit server.properties
```shell
vi etc/kafka/server.properties
```

2-2. Set server.properties for localhost setting
```txt
############################# Socket Server Settings #############################

# The address the socket server listens on. If not configured, the host name will be equal to the value of
# java.net.InetAddress.getCanonicalHostName(), with PLAINTEXT listener name, and port 9092.
#   FORMAT:
#     listeners = listener_name://host_name:port
#   EXAMPLE:
#     listeners = PLAINTEXT://your.host.name:9092
listeners=PLAINTEXT://0.0.0.0:9092

# Listener name, hostname and port the broker will advertise to clients.
# If not set, it uses the value for "listeners".
advertised.listeners=PLAINTEXT://localhost:9092

# Maps listener names to security protocols, the default is for them to be the same. See the config documentation for more details
listener.security.protocol.map=PLAINTEXT:PLAINTEXT,SSL:SSL,SASL_PLAINTEXT:SASL_PLAINTEXT,SASL_SSL:SASL_SSL
```

3. Set environment variable about confluent kafka home directory

```shell
# Confluent Kafka Path
export CONFLUENT_HOME=/{CONFLUENT_KAFKA_DIR_PATH}/confluent-7.6.0
export PATH=$PATH:$CONFLUENT_HOME/bin
```


```shell
source .zshrc
```

4. Start confluent kafka

```shell
arch -x86_64 confluent local services start
```

5. Check confluent kafka is running

```curl
http://localhost:9091
```