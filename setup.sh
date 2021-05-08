#! /bin/bash

set -e

cd bootstrap/spark-with-hadoop/

# build hive-postgres metastore image
{
  echo "[$(date)]        INFO:[+]Building image: hive-metastore"
  docker build . -t hive-metastore:latest -f hive-metastore/Dockerfile
} && echo "[$(date)]        INFO:[+]Building image hive-metastore: SUCCESS" || {
  echo "[$(date)]        ERROR:[+]Building image for hive-metastore :FAILED"
  exit 1
}

# build spark-hadoop standalone metastore image
{
  echo "[$(date)]        INFO:[+]Building image: spark-hadoop-standalone"
  docker build . -t spark-standalone-hadoop:latest -f spark-hadoop-standalone/Dockerfile
} && echo "[$(date)]        INFO:[+]Building image spark-hadoop-standalone: SUCCESS" || {
  echo "[$(date)]        ERROR:[+]Building image for spark-hadoop-standalone :FAILED"
  exit 1
}

#spinning docker containers using compose
docker-compose up -d

sleep 60

#starting hadoop
echo "Starting services"
docker exec -it spark bash -c "hdfs namenode -format && start-dfs.sh && hdfs dfs -mkdir -p /tmp && hdfs dfs -mkdir -p /user/hive/warehouse && hdfs dfs -chmod g+w /user/hive/warehouse" &&
  docker exec -d spark bash -c "hive --service metastore && hive --service hiveserver2" &&
  docker exec -it spark bash -c "rm -f /etc/yum.repos.d/bintray-rpm.repo &&  curl -L https://www.scala-sbt.org/sbt-rpm.repo > sbt-rpm.repo && mv sbt-rpm.repo /etc/yum.repos.d/ && yum install -y sbt && cd /opt && git clone https://github.com/AnudeepKonaboina/spear-framework.git && cd spear-framework && sbt package" &&
  docker exec -it spark bash -c "cd /opt/spear-framework/target/scala-* && spark-shell --jars *.jar --packages  org.postgresql:postgresql:9.4.1211,org.apache.hadoop:hadoop-aws:2.10.1,org.apache.spark:spark-hive_2.11:2.4.7,org.apache.spark:spark-avro_2.11:2.4.7,org.apache.hadoop:hadoop-azure:2.10.1,org.apache.hadoop:hadoop-azure-datalake:2.10.1,com.google.cloud.spark:spark-bigquery-with-dependencies_2.11:0.18.1"
