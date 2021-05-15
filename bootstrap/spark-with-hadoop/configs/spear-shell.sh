#!/usr/bin/env bash

sed '92 i figlet -f slant -w 100 SPEAR FRAMEWORK' /usr/bin/spark-2.4.7-bin-without-hadoop/bin/spark-shell

cd /opt/spear-framework/target/scala-* &&
spark-shell --conf "spark.sql.parquet.writeLegacyFormat=true" --jars spear-framework_2.11-0.1.0-SNAPSHOT.jar,ojdbc6.jar,gcs-connector-hadoop2-latest.jar --packages org.postgresql:postgresql:9.4.1211,org.apache.hadoop:hadoop-aws:2.10.1,org.apache.spark:spark-hive_2.11:2.4.7,org.apache.spark:spark-avro_2.11:2.4.7,org.apache.hadoop:hadoop-azure:2.10.1,org.apache.hadoop:hadoop-azure-datalake:2.10.1,com.google.cloud.spark:spark-bigquery-with-dependencies_2.11:0.18.1