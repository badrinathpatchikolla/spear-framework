#!/usr/bin/env bash

echo "Starting spark-shell with spear-framework ...."

cd /opt/spear-framework/target/scala-* &&
spark-shell --conf "spark.sql.parquet.writeLegacyFormat=true" --jars spear-framework-1.0.jar,ojdbc6.jar,gcs-connector-hadoop2-latest.jar --packages org.postgresql:postgresql:9.4.1211,org.apache.hadoop:hadoop-aws:2.10.1,org.apache.spark:spark-hive_2.11:2.4.7,org.apache.spark:spark-avro_2.11:2.4.7,org.apache.hadoop:hadoop-azure:2.10.1,org.apache.hadoop:hadoop-azure-datalake:2.10.1,com.google.cloud.spark:spark-bigquery-with-dependencies_2.11:0.18.1,com.springml:spark-salesforce_2.11:1.1.3
