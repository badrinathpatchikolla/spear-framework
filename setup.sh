#! /bin/bash


set -e

cd bootstrap/spark-with-hadoop/ || exit 1

buildServiceImages() {
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
}

setup() {
  #spinning docker containers using compose
  docker-compose up -d
  sleep 60
  #starting hadoop and hive services
  docker exec -it spark bash -c "hdfs namenode -format && start-dfs.sh && hdfs dfs -mkdir -p /tmp && hdfs dfs -mkdir -p /user/hive/warehouse && hdfs dfs -chmod g+w /user/hive/warehouse" &&
    docker exec -d spark bash -c "hive --service metastore && hive --service hiveserver2"  &&
    docker exec -it spark bash -c "chmod u+x /root/spear-shell.sh && wget https://mirrors.estointernet.in/apache/kafka/2.7.0/kafka_2.13-2.7.0.tgz -O /tmp/kafka.tgz && cd / && tar -xvf /tmp/kafka.tgz -C / && mv kafka_2.13-2.7.0 kafka && rm -f /etc/yum.repos.d/bintray-rpm.repo &&  curl -L https://www.scala-sbt.org/sbt-rpm.repo > sbt-rpm.repo && mv sbt-rpm.repo /etc/yum.repos.d/ && yum install -y sbt && cd /opt && git clone -b feature/dev https://github.com/AnudeepKonaboina/spear-framework.git && cd spear-framework && sbt package"
}

buildServiceImages && setup
