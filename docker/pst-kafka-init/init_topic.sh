#!/usr/bin/env sh

echo "Install bash"
apk add bash

echo "Install openjdk 25"
apk add openjdk25
export JAVA_HOME="/usr/lib/jvm/java-25-openjdk"

cd /opt

echo "Checking URL: $KAFKA_CLIENT";
status=$(wget --spider --server-response "$KAFKA_CLIENT" 2>&1 | grep "HTTP/" | tail -1);
#echo "Result: [$status]";
txt_status=$(echo "$status" | awk -F ' ' '{print $3}');
#echo "Text status: $txt_status";

if [ "$txt_status" == "OK" ]; then
  echo "Downloading $KAFKA_CLIENT"
  wget "${KAFKA_CLIENT}"
  ARCH=$(echo ${KAFKA_CLIENT} | awk -F/ '{print $6}');

  echo "Extracting archive $ARCH"
  tar -zxf $ARCH

  fname=$(echo "${ARCH%.*}");
  export PATH="$PATH:/opt/$fname/bin";

  /opt/$fname/bin/kafka-topics.sh --bootstrap-server=pst-kafka:19092 --create --if-not-exists --topic ${TOPIC} --replication-factor 1 --partitions 2

else
  echo "Invalid url: [$KAFKA_CLIENT] with status [$status]"
fi