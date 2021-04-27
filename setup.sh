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
  docker exec -it spark bash -c "curl https://bintray.com/sbt/rpm/rpm |  tee /etc/yum.repos.d/bintray-sbt-rpm.repo && yum install -y sbt && cd /opt && git clone https://github.com/AnudeepKonaboina/spear-framework.git && cd spear-framework && sbt package" &&
  docker exec -it spark bash -c "cd /opt/spear-framework/target/scala-2.12 && spark-shell --jars spear-framework_2.12-0.1.jar --packages org.apache.spark:spark-hive_2.11:2.4.0"
