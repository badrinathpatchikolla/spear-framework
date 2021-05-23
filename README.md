# Spear Framework

[![Build Status](https://github.com/AnudeepKonaboina/spear-framework/workflows/spear-framework-build/badge.svg)](https://github.com/AnudeepKonaboina/spear-framework/actions)
[![Code Quality Grade](https://www.code-inspector.com/project/22855/status/svg)](https://www.code-inspector.com/project/22855/status/svg)
[![Code Quality Score](https://www.code-inspector.com/project/22855/score/svg)](https://www.code-inspector.com/project/22855/score/svg)
[![GitHub tag](https://img.shields.io/github/v/release/AnudeepKonaboina/spear-framework)](https://GitHub.com/AnudeepKonaboina/spear-framework/tags/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

The spear-framework provides scope to write simple ETL-connectors/pipelines for moving data from different sources to different destinations which greatly minimizes the effort of writing complex codes for data ingestion. Connectors which have the ability to extract and load (ETL or ELT) any kind of data from source with custom tansformations applied can be written and executed seamlessly using spear connectors.


## Table of Contents
- [Introduction](#introduction)
- [Pre-Requisites](#pre-requisites)
- [Getting started with Spear](#getting-started-with-spear)
- [Build your first connector](#build-your-first-connector)
- [Example Connectors](#example-connectors)
    * [Target JDBC](#target-jdbc)
        - [File Source](#file-source)
            + [CSV to JDBC Connector](#csv-to-jdbc-connector)
            + [JSON to JDBC Connector](#json-to-jdbc-connector)
            + [XML to JDBC Connector](#xml-to-jdbc-connector)
            + [Avro to JDBC Connector](#avro-to-jdbc-connector)
            + [Parquet to JDBC Connector](#parquet-to-jdbc-connector)
        - [JDBC Source](#jdbc-source)
            + [Oracle to Postgres Connector](#oracle-to-postgres-connector)
            + [Postgres to Salesforce Connector](#postgres-to-salesforce-connector)
        - [Streaming Source](#streaming-source)
            + [kafka to Postgres Connector](#kafka-to-postgres-connector)
    * [Target FS (HDFS)](#target-fs-hdfs)
        - [JDBC Source](#jdbc-source)
            + [Postgres to Hive Connector](#postgres-to-hive-connector)
            + [Oracle to Hive Connector](#oracle-to-hive-connector)
            + [Salesforce to Hive Connector](#salesforce-to-hive-connector)
        - [Streaming Source](#streaming-source)
            + [kafka to Hive Connector](#kafka-to-hive-connector)
    * [Target FS (Cloud)](#target-fs-cloud)
        + [Oracle to S3 Connector](#oracle-to-s3-connector)
        + [Postgres to GCS Connector](#postgres-to-gcs-connector)
- [Contributions and License](#contributions-and-license)

## Introduction

Spear Framework is basically used to write connectors (ETL jobs) from a source to a target,applying business logic/transformations over the soure data and ingesting it to the corresponding destination with minimal code.

![image](https://user-images.githubusercontent.com/59328701/118606240-cf12d600-b7d4-11eb-9d9f-c308b3ef286c.png)

## Pre-Requisites
Following are the pre-requisites you need for playing around with spear:

1. Need to have a linux machine with 16GB Ram and 4 CPU's for better performance
2. Install docker and docker-compose using the below steps
```commandline
#To install docker on centos:
============================
sudo yum remove docker \
                  docker-client \
                  docker-client-latest \
                  docker-common \
                  docker-latest \
                  docker-latest-logrotate \
                  docker-logrotate \
                  docker-engine
sudo yum install -y yum-utils
sudo yum-config-manager \
    --add-repo \
    https://download.docker.com/linux/centos/docker-ce.repo

sudo yum install docker-ce docker-ce-cli containerd.io
sudo systemctl start docker

#To install docker ubuntu:
=========================
sudo apt-get remove docker docker-engine docker.io containerd runc

sudo apt-get update
sudo apt-get install \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg \
    lsb-release
    
sudo apt-get install docker.io
sudo systemctl start docker

#install docker-compose:
=======================
sudo curl -L "https://github.com/docker/compose/releases/download/1.28.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

## Getting Started with Spear

Below are the steps to setup spear on any machine having docker and docker-compose installed:

1. Clone the repository from git and navigate to project directory
```commandline
git clone https://github.com/AnudeepKonaboina/spear-framework.git && cd spear-framework
```

2. Run setup.sh script using the command
```commandline
sh setup.sh
```

3. Once the setup is completed run the below commands for starting spear-framework on spark:
```commandline
->Enter into spark-conatiner using the comamnd:
user@node~$ docker exec -it spark bash

->Run `spear-shell` to start the shell:
root@hadoop # spear-shell
```

**NOTE**: This spark shell is encpsulated with default hadoop/hive environment readily availble to read data from any source
and write it to HDFS so that it gives you complete environment to play with spear-framework.

4. Start writing your own single line connectors and explore .To understand how to write a connector [click here](#how-to-write-a-connector)


## Build your first connector
Below are the steps to write any connector:

1. Get the suitable connector object using Spearconnector by providing the source and destination details as shown below:
```commandline
import com.github.edge.roman.spear.SpearConnector

val connector = SpearConnector
  .createConnector(name="defaultconnector")//give a name to your connector(any name)
  .source(sourceType = "relational", sourceFormat = "jdbc")//source type and format for loading data  
  .target(targetType = "FS", targetFormat = "parquet")//target type and format for writing data to dest.
  .getConnector 

Below table shows all the supported source and destination types.
```
Below are the source and destination type combinations that spear-framework supports:
```commandline
|source type  | dest. type    | description                                                | 
|------------ |:-------------:|:-----------------------------------------------------------:
| file        |  relational   |connector object with file source and database as dest.     |
| relational  |  relational   |connector object with database source and database as dest. |
| stream      |  relational   |connector object with stream source and database as dest.   |
| file        |  FS           |connector object with file source and FileSystem as dest.   |
| relational  |  FS           |connector object with database source and FileSystem as dest|
| stream      |  FS           |connector object with stream source and FileSystem as dest. |
| FS          |  FS           |connector object with FileS  source and FileSystem as dest. |
```


2. Write the connector logic using the connector object.

```commandline
connector
   //souce object and connection profile needs to be specified
  .source(sourceObject="can be <filename/tablename/topic/api>", <connection profile Map((key->value))>)
  //creates a temporary table on the source data with the given alias name which can be used for further transformations
  .saveAs("<alias temporary table name>")
  //apply custom tranformations on the loaded source data.(optional/can be applied only if necessary)
  .transformSql("<transformations to be applied on source data>")
  //target details where you want to load the data.
  .targetFS(destinationFilePath = "<hdfs /s3/gcs file path>", saveAsTable = "<tablename>", <Savemode can be overwrite/append/ignore>)
```

3. On completion stop the connector.

```commandline
//stops the connector object
connector.stop()
```

4. Enable verbose logging 
To get the output df at each stage in your connector you can explicitly enable verbose logging as below on top of connector object.

```commandline
connector.setVeboseLogging(true) //default value is false.
```

## Example Connectors
Connector is basically the logic/code which allows you to create a pipeline from source to target using the spear framework using which you can ingest data from any source to any destination.

### Target JDBC
Spear framework supports writing data to any RDBMS with jdbc as destination(postgres/oracle/msql etc..)  from various sources like a file(csv/json/filesystem etc..)/database(RDBMS/cloud db etc..)/streaming(kafka/dir path etc..).Given below are examples of few connectors with JDBC as target.Below examples are written for postgresql as JDBC target,but this can be extended for any jdbc target.

### File source

#### CSV to JDBC Connector
An example connector for reading csv file applying transformations and storing it into postgres table using spear:\

The input data is available in the data/us-election-2012-results-by-county.csv. Simply copy the below connector and paste it in your interactive shell and see your data being moved to a table in postgres with such minimal code !!!.

```scala
import com.github.edge.roman.spear.SpearConnector
import org.apache.log4j.{Level, Logger}
import java.util.Properties
import org.apache.spark.sql.{Column, DataFrame, SaveMode}

val properties = new Properties()
  properties.put("driver", "org.postgresql.Driver");
  properties.put("user", "postgres_user")
  properties.put("password", "mysecretpassword")
  properties.put("url", "jdbc:postgresql://postgres:5432/pgdb")

//create a connector object
val csvJdbcConnector = SpearConnector
    .createConnector(name="CSVtoPostgresConnector")
    .source(sourceType = "file", sourceFormat = "csv")
    .target(targetType = "relational", targetFormat = "jdbc")
    .getConnector
    
//enable verbose logging i.e.., the output after every stage is displayed in the console
csvJdbcConnector.setVeboseLogging(true)

//connector logic
csvJdbcConnector
  .source(sourceObject="file:///opt/spear-framework/data/us-election-2012-results-by-county.csv", Map("header" -> "true", "inferSchema" -> "true"))
  .saveAs("__tmp__")
  .transformSql(
    """select state_code,party,
      |sum(votes) as total_votes
      |from __tmp__
      |group by state_code,party""".stripMargin)
  .targetJDBC(tableName="mytable", properties, SaveMode.Overwrite)
csvJdbcConnector.stop()
```

##### Output:

```

21/05/20 16:17:37 INFO targetjdbc.FiletoJDBC: Reading source file: file:///opt/spear-framework/data/us-election-2012-results-by-county.csv with format: csv status:success
+----------+----------+------------+-------------------+-----+----------+---------+-----+
|country_id|state_code|country_name|country_total_votes|party|first_name|last_name|votes|
+----------+----------+------------+-------------------+-----+----------+---------+-----+
|1         |AK        |Alasaba     |220596             |Dem  |Barack    |Obama    |91696|
|2         |AK        |Akaskak     |220596             |Dem  |Barack    |Obama    |91696|
|3         |AL        |Autauga     |23909              |Dem  |Barack    |Obama    |6354 |
|4         |AK        |Akaska      |220596             |Dem  |Barack    |Obama    |91696|
|5         |AL        |Baldwin     |84988              |Dem  |Barack    |Obama    |18329|
|6         |AL        |Barbour     |11459              |Dem  |Barack    |Obama    |5873 |
|7         |AL        |Bibb        |8391               |Dem  |Barack    |Obama    |2200 |
|8         |AL        |Blount      |23980              |Dem  |Barack    |Obama    |2961 |
|9         |AL        |Bullock     |5318               |Dem  |Barack    |Obama    |4058 |
|10        |AL        |Butler      |9483               |Dem  |Barack    |Obama    |4367 |
+----------+----------+------------+-------------------+-----+----------+---------+-----+
only showing top 10 rows

21/05/20 16:17:39 INFO targetjdbc.FiletoJDBC: Saving data as temporary table:__tmp__ success
21/05/20 16:17:39 INFO targetjdbc.FiletoJDBC: Executing tranformation sql: select state_code,party,
sum(votes) as total_votes
from __tmp__
group by state_code,party status :success
+----------+-----+-----------+
|state_code|party|total_votes|
+----------+-----+-----------+
|AL        |Dem  |793620     |
|NY        |GOP  |2226637    |
|MI        |CST  |16792      |
|ID        |GOP  |420750     |
|ID        |Ind  |2495       |
|WA        |CST  |7772       |
|HI        |Grn  |3121       |
|MS        |RP   |969        |
|MN        |Grn  |13045      |
|ID        |Dem  |212560     |
+----------+-----+-----------+
only showing top 10 rows

21/05/20 16:17:56 INFO targetjdbc.FiletoJDBC: Write data to table/object mytable completed with status:success
+----------+-----+-----------+
|state_code|party|total_votes|
+----------+-----+-----------+
|AL        |Dem  |793620     |
|NY        |GOP  |2226637    |
|MI        |CST  |16792      |
|ID        |GOP  |420750     |
|ID        |Ind  |2495       |
|WA        |CST  |7772       |
|HI        |Grn  |3121       |
|MS        |RP   |969        |
|MN        |Grn  |13045      |
|ID        |Dem  |212560     |
+----------+-----+-----------+
only showing top 10 rows
```

#### JSON to JDBC Connector

Connector for reading json file applying transformations and storing it into postgres table using spear:\
The input data is available in the data/data.json

```scala
import com.github.edge.roman.spear.SpearConnector
import org.apache.spark.sql.SaveMode
import java.util.Properties

//target props
val properties = new Properties()
properties.put("driver", "org.postgresql.Driver");
properties.put("user", "postgres_user")
properties.put("password", "mysecretpassword")
properties.put("url", "jdbc:postgresql://postgres_host:5433/pg_db")

val jsonJdbcConnector = SpearConnector
  .createConnector(name="JSONtoPostgresConnector")
  .source(sourceType = "file", sourceFormat = "json")
  .target(targetType = "relational", targetFormat="jdbc")
  .getConnector

jsonJdbcConnector.setVeboseLogging(true)

jsonJdbcConnector
  .source("file:///opt/spear-framework/data/data.json", Map("multiline" -> "true"))
  .saveAs("__tmptable__")
  .transformSql("select cast(id*10 as integer) as type_id,type from __tmptable__ ")
  .targetJDBC(tableName = "json_to_jdbc", properties, SaveMode.Overwrite)
jsonJdbcConnector.stop()
```

##### Output

```
21/05/20 16:25:54 INFO targetjdbc.FiletoJDBC: Reading source file: file:///opt/spear-framework/data/data.json with format: json status:success
+----+------------------------+
|id  |type                    |
+----+------------------------+
|5001|None                    |
|5002|Glazed                  |
|5005|Sugar                   |
|5007|Powdered Sugar          |
|5006|Chocolate with Sprinkles|
|5003|Chocolate               |
|5004|Maple                   |
+----+------------------------+

21/05/20 16:25:55 INFO targetjdbc.FiletoJDBC: Saving data as temporary table:__tmptable__ success
21/05/20 16:25:55 INFO targetjdbc.FiletoJDBC: Executing tranformation sql: select cast(id*10 as integer) as type_id,type from __tmptable__  status :success
+-------+------------------------+
|type_id|type                    |
+-------+------------------------+
|50010  |None                    |
|50020  |Glazed                  |
|50050  |Sugar                   |
|50070  |Powdered Sugar          |
|50060  |Chocolate with Sprinkles|
|50030  |Chocolate               |
|50040  |Maple                   |
+-------+------------------------+

21/05/20 16:25:58 INFO targetjdbc.FiletoJDBC: Write data to table/object json_to_jdbc completed with status:success
+-------+------------------------+
|type_id|type                    |
+-------+------------------------+
|50010  |None                    |
|50020  |Glazed                  |
|50050  |Sugar                   |
|50070  |Powdered Sugar          |
|50060  |Chocolate with Sprinkles|
|50030  |Chocolate               |
|50040  |Maple                   |
+-------+------------------------+

```

#### XML to JDBC Connector

Connector for reading xml file applying transformations and storing it into postgres table using spear:\
The input data is available in the data/data.xml

```scala
import com.github.edge.roman.spear.SpearConnector
import java.util.Properties
import org.apache.spark.sql.{Column, DataFrame, SaveMode}

//target props
val properties = new Properties()
properties.put("driver", "org.postgresql.Driver");
properties.put("user", "postgres_user")
properties.put("password", "mysecretpassword")
properties.put("url", "jdbc:postgresql://postgres:5432/pgdb")

val xmlJdbcConnector = SpearConnector
  .createConnector(name="JSONtoPostgresConnector")
  .source(sourceType = "file", sourceFormat = "xml")
  .target(targetType = "relational", targetFormat = "jdbc")
  .getConnector

xmlJdbcConnector.setVeboseLogging(true)

xmlJdbcConnector
  .source("file:///opt/spear-framework/data/data.xml", Map("rootTag" -> "employees", "rowTag" -> "details"))
  .saveAs("tmp")
  .transformSql("select * from tmp ")
  .targetJDBC(tableName="xml_to_jdbc", properties, SaveMode.Overwrite)
xmlJdbcConnector.stop()
```

##### Output

```
21/05/20 16:30:31 INFO targetjdbc.FiletoJDBC: Reading source file: file:///opt/spear-framework/data/data.xml with format: xml status:success
+--------+--------+---------+--------+----+---------+
|building|division|firstname|lastname|room|title    |
+--------+--------+---------+--------+----+---------+
|301     |Computer|Shiv     |Mishra  |11  |Engineer |
|303     |Computer|Yuh      |Datta   |2   |developer|
|304     |Computer|Rahil    |Khan    |10  |Tester   |
|305     |Computer|Deep     |Parekh  |14  |Designer |
+--------+--------+---------+--------+----+---------+

21/05/20 16:30:32 INFO targetjdbc.FiletoJDBC: Saving data as temporary table:tmp success
21/05/20 16:30:32 INFO targetjdbc.FiletoJDBC: Executing tranformation sql: select * from tmp  status :success
+--------+--------+---------+--------+----+---------+
|building|division|firstname|lastname|room|title    |
+--------+--------+---------+--------+----+---------+
|301     |Computer|Shiv     |Mishra  |11  |Engineer |
|303     |Computer|Yuh      |Datta   |2   |developer|
|304     |Computer|Rahil    |Khan    |10  |Tester   |
|305     |Computer|Deep     |Parekh  |14  |Designer |
+--------+--------+---------+--------+----+---------+

21/05/20 16:30:36 INFO targetjdbc.FiletoJDBC: Write data to table/object xml_to_jdbc completed with status:success
+--------+--------+---------+--------+----+---------+
|building|division|firstname|lastname|room|title    |
+--------+--------+---------+--------+----+---------+
|301     |Computer|Shiv     |Mishra  |11  |Engineer |
|303     |Computer|Yuh      |Datta   |2   |developer|
|304     |Computer|Rahil    |Khan    |10  |Tester   |
|305     |Computer|Deep     |Parekh  |14  |Designer |
+--------+--------+---------+--------+----+---------+
```


#### Avro to JDBC Connector

Connector for reading avro file applying transformations and storing it into postgres table using spear:\
The input data is available in the data/sample_data.avro

```scala
import com.github.edge.roman.spear.SpearConnector
import org.apache.spark.sql.SaveMode
import scala.collection.JavaConverters._
import java.util.Properties

val properties = new Properties()
properties.put("driver", "org.postgresql.Driver");
properties.put("user", "postgres")
properties.put("password", "pass")
properties.put("url", "jdbc:postgresql://localhost:5432/pgdb")

val avroJdbcConnector = SpearConnector
    .createConnector(name = "AvrotoPostgresConnector")
    .source(sourceType = "file", sourceFormat = "avro")
    .target(targetType = "relational", targetFormat = "jdbc")
    .getConnector
avroJdbcConnector.setVeboseLogging(true)
avroJdbcConnector
    .source(sourceObject = "file:///opt/spear-framework/data/sample_data.avro")
    .saveAs("__tmp__")
    .transformSql(
      """select id,
        |cast(concat(first_name ,' ', last_name) as VARCHAR(255)) as name,
        |coalesce(gender,'NA') as gender,
        |cast(country as VARCHAR(20)) as country,
        |cast(salary as DOUBLE) as salary,email
        |from __tmp__""".stripMargin)
    .saveAs("__transformed_table__")
    .transformSql(
      """
        |select id,name,
        |country,email,
        |salary
        |from __transformed_table__""".stripMargin)
    .targetJDBC(tableName = "avro_data", properties, SaveMode.Overwrite)  

avroJdbcConnector.stop()
```

##### Output

```
21/05/20 16:55:42 INFO targetjdbc.FiletoJDBC: Reading source file: file:///opt/spear-framework/data/sample_data.avro with format: avro status:success
+--------------------+---+----------+---------+------------------------+------+--------------+----------------+----------------------+----------+---------+------------------------+--------+
|registration_dttm   |id |first_name|last_name|email                   |gender|ip_address    |cc              |country               |birthdate |salary   |title                   |comments|
+--------------------+---+----------+---------+------------------------+------+--------------+----------------+----------------------+----------+---------+------------------------+--------+
|2016-02-03T07:55:29Z|1  |Amanda    |Jordan   |ajordan0@com.com        |Female|1.197.201.2   |6759521864920116|Indonesia             |3/8/1971  |49756.53 |Internal Auditor        |1E+02   |
|2016-02-03T17:04:03Z|2  |Albert    |Freeman  |afreeman1@is.gd         |Male  |218.111.175.34|null            |Canada                |1/16/1968 |150280.17|Accountant IV           |        |
|2016-02-03T01:09:31Z|3  |Evelyn    |Morgan   |emorgan2@altervista.org |Female|7.161.136.94  |6767119071901597|Russia                |2/1/1960  |144972.51|Structural Engineer     |        |
|2016-02-03T12:36:21Z|4  |Denise    |Riley    |driley3@gmpg.org        |Female|140.35.109.83 |3576031598965625|China                 |4/8/1997  |90263.05 |Senior Cost Accountant  |        |
|2016-02-03T05:05:31Z|5  |Carlos    |Burns    |cburns4@miitbeian.gov.cn|      |169.113.235.40|5602256255204850|South Africa          |          |null     |                        |        |
|2016-02-03T07:22:34Z|6  |Kathryn   |White    |kwhite5@google.com      |Female|195.131.81.179|3583136326049310|Indonesia             |2/25/1983 |69227.11 |Account Executive       |        |
|2016-02-03T08:33:08Z|7  |Samuel    |Holmes   |sholmes6@foxnews.com    |Male  |232.234.81.197|3582641366974690|Portugal              |12/18/1987|14247.62 |Senior Financial Analyst|        |
|2016-02-03T06:47:06Z|8  |Harry     |Howell   |hhowell7@eepurl.com     |Male  |91.235.51.73  |null            |Bosnia and Herzegovina|3/1/1962  |186469.43|Web Developer IV        |        |
|2016-02-03T03:52:53Z|9  |Jose      |Foster   |jfoster8@yelp.com       |Male  |132.31.53.61  |null            |South Korea           |3/27/1992 |231067.84|Software Test Engineer I|1E+02   |
|2016-02-03T18:29:47Z|10 |Emily     |Stewart  |estewart9@opensource.org|Female|143.28.251.245|3574254110301671|Nigeria               |1/28/1997 |27234.28 |Health Coach IV         |        |
+--------------------+---+----------+---------+------------------------+------+--------------+----------------+----------------------+----------+---------+------------------------+--------+
only showing top 10 rows

21/05/20 16:55:43 INFO targetjdbc.FiletoJDBC: Saving data as temporary table:__tmp__ success
21/05/20 16:55:43 INFO targetjdbc.FiletoJDBC: Executing tranformation sql: select id,
cast(concat(first_name ,' ', last_name) as VARCHAR(255)) as name,
coalesce(gender,'NA') as gender,
cast(country as VARCHAR(20)) as country,
cast(salary as DOUBLE) as salary,email
from __tmp__ status :success
+---+--------------+------+----------------------+---------+------------------------+
|id |name          |gender|country               |salary   |email                   |
+---+--------------+------+----------------------+---------+------------------------+
|1  |Amanda Jordan |Female|Indonesia             |49756.53 |ajordan0@com.com        |
|2  |Albert Freeman|Male  |Canada                |150280.17|afreeman1@is.gd         |
|3  |Evelyn Morgan |Female|Russia                |144972.51|emorgan2@altervista.org |
|4  |Denise Riley  |Female|China                 |90263.05 |driley3@gmpg.org        |
|5  |Carlos Burns  |      |South Africa          |null     |cburns4@miitbeian.gov.cn|
|6  |Kathryn White |Female|Indonesia             |69227.11 |kwhite5@google.com      |
|7  |Samuel Holmes |Male  |Portugal              |14247.62 |sholmes6@foxnews.com    |
|8  |Harry Howell  |Male  |Bosnia and Herzegovina|186469.43|hhowell7@eepurl.com     |
|9  |Jose Foster   |Male  |South Korea           |231067.84|jfoster8@yelp.com       |
|10 |Emily Stewart |Female|Nigeria               |27234.28 |estewart9@opensource.org|
+---+--------------+------+----------------------+---------+------------------------+
only showing top 10 rows

21/05/20 16:55:45 INFO targetjdbc.FiletoJDBC: Saving data as temporary table:__transformed_table__ success
21/05/20 16:55:45 INFO targetjdbc.FiletoJDBC: Executing tranformation sql:
select id,name,
country,email,
salary
from __transformed_table__ status :success
+---+--------------+----------------------+------------------------+---------+
|id |name          |country               |email                   |salary   |
+---+--------------+----------------------+------------------------+---------+
|1  |Amanda Jordan |Indonesia             |ajordan0@com.com        |49756.53 |
|2  |Albert Freeman|Canada                |afreeman1@is.gd         |150280.17|
|3  |Evelyn Morgan |Russia                |emorgan2@altervista.org |144972.51|
|4  |Denise Riley  |China                 |driley3@gmpg.org        |90263.05 |
|5  |Carlos Burns  |South Africa          |cburns4@miitbeian.gov.cn|null     |
|6  |Kathryn White |Indonesia             |kwhite5@google.com      |69227.11 |
|7  |Samuel Holmes |Portugal              |sholmes6@foxnews.com    |14247.62 |
|8  |Harry Howell  |Bosnia and Herzegovina|hhowell7@eepurl.com     |186469.43|
|9  |Jose Foster   |South Korea           |jfoster8@yelp.com       |231067.84|
|10 |Emily Stewart |Nigeria               |estewart9@opensource.org|27234.28 |
+---+--------------+----------------------+------------------------+---------+
only showing top 10 rows

21/05/20 16:55:48 INFO targetjdbc.FiletoJDBC: Write data to table/object avro_data completed with status:success
+---+--------------+----------------------+------------------------+---------+
|id |name          |country               |email                   |salary   |
+---+--------------+----------------------+------------------------+---------+
|1  |Amanda Jordan |Indonesia             |ajordan0@com.com        |49756.53 |
|2  |Albert Freeman|Canada                |afreeman1@is.gd         |150280.17|
|3  |Evelyn Morgan |Russia                |emorgan2@altervista.org |144972.51|
|4  |Denise Riley  |China                 |driley3@gmpg.org        |90263.05 |
|5  |Carlos Burns  |South Africa          |cburns4@miitbeian.gov.cn|null     |
|6  |Kathryn White |Indonesia             |kwhite5@google.com      |69227.11 |
|7  |Samuel Holmes |Portugal              |sholmes6@foxnews.com    |14247.62 |
|8  |Harry Howell  |Bosnia and Herzegovina|hhowell7@eepurl.com     |186469.43|
|9  |Jose Foster   |South Korea           |jfoster8@yelp.com       |231067.84|
|10 |Emily Stewart |Nigeria               |estewart9@opensource.org|27234.28 |
+---+--------------+----------------------+------------------------+---------+
only showing top 10 rows

```

#### Parquet to JDBC Connector

Connector for reading parquet file applying transformations and storing it into postgres table using spear:\
The input data is available in the data/sample.parquet

```scala
import com.github.edge.roman.spear.SpearConnector
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SaveMode

val properties = new Properties()
properties.put("driver", "org.postgresql.Driver");
properties.put("user", "postgres")
properties.put("password", "pass")
properties.put("url", "jdbc:postgresql://localhost:5432/pgdb")

val parquetJdbcConnector = SpearConnector
  .createConnector(name="ParquettoPostgresConnector")
  .source(sourceType="file", sourceFormat="parquet")
  .target(targetType="relational", targetFormat="jdbc")
  .getConnector

parquetJdbcConnector
  .source("file:///opt/spear-framework/data/sample.parquet")
  .saveAs("__tmp__")
  .transformSql("""select flow1,occupancy1,speed1 from __tmp__""")
  .targetJDBC(tableName="user_data", properties, SaveMode.Overwrite

parquetJdbcConnector.stop()
```

### Output

```
21/05/20 17:00:09 INFO targetjdbc.FiletoJDBC: Reading source file: file:///opt/spear-framework/data/sample.parquet with format: parquet status:success
21/05/20 17:00:09 INFO targetjdbc.FiletoJDBC: Saving data as temporary table:__tmp__ success
21/05/20 17:00:09 INFO targetjdbc.FiletoJDBC: Executing tranformation sql: select flow1,occupancy1,speed1 from __tmp__ status :success
21/05/20 17:00:16 INFO targetjdbc.FiletoJDBC: Write data to table/object user_data completed with status:success

```

### JDBC source

#### Oracle to Postgres Connector

```scala
import com.github.edge.roman.spear.SpearConnector
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SaveMode
import java.util.Properties

val properties = new Properties()
properties.put("driver", "org.postgresql.Driver");
properties.put("user", "postgres")
properties.put("password", "pass")
properties.put("url", "jdbc:postgresql://localhost:5432/pgdb")

Logger.getLogger("com.github").setLevel(Level.INFO)

val oracleTOPostgresConnector = SpearConnector
  .createConnector(name="OracletoPostgresConnector")
  .source(sourceType = "relational", sourceFormat = "jdbc")
  .target(targetType = "relational", targetFormat = "jdbc")
  .getConnector

oracleTOPostgresConnector.setVeboseLogging(true)

oracleTOPostgresConnector
  .sourceSql(Map("driver" -> "oracle.jdbc.driver.OracleDriver", "user" -> "user", "password" -> "pass", "url" -> "jdbc:oracle:thin:@ora-host:1521:orcl"),
    """
      |SELECT
      |        to_char(sys_extract_utc(systimestamp), 'YYYY-MM-DD HH24:MI:SS.FF') as ingest_ts_utc,
      |        to_char(TIMESTAMP_0, 'YYYY-MM-DD HH24:MI:SS.FF') as timestamp_0,
      |        to_char(TIMESTAMP_5, 'YYYY-MM-DD HH24:MI:SS.FF') as timestamp_5,
      |        to_char(TIMESTAMP_7, 'YYYY-MM-DD HH24:MI:SS.FF') as timestamp_7,
      |        to_char(TIMESTAMP_9, 'YYYY-MM-DD HH24:MI:SS.FF') as timestamp_9,
      |        to_char(TIMESTAMP0_WITH_TZ) as timestamp0_with_tz , to_char(sys_extract_utc(TIMESTAMP0_WITH_TZ), 'YYYY-MM-DD HH24:MI:SS') as timestamp0_with_tz_utc,
      |        to_char(TIMESTAMP5_WITH_TZ) as timestamp5_with_tz , to_char(sys_extract_utc(TIMESTAMP5_WITH_TZ), 'YYYY-MM-DD HH24:MI:SS.FF') as timestamp5_with_tz_utc,
      |        to_char(TIMESTAMP8_WITH_TZ) as timestamp8_with_tz , to_char(sys_extract_utc(TIMESTAMP8_WITH_TZ), 'YYYY-MM-DD HH24:MI:SS.FF') as timestamp8_with_tz_utc,
      |        to_char(TIMESTAMP0_WITH_LTZ) as timestamp0_with_ltz , to_char(sys_extract_utc(TIMESTAMP0_WITH_LTZ), 'YYYY-MM-DD HH24:MI:SS') as timestamp0_with_ltz_utc,
      |        to_char(TIMESTAMP5_WITH_LTZ) as timestamp5_with_ltz , to_char(sys_extract_utc(TIMESTAMP5_WITH_LTZ), 'YYYY-MM-DD HH24:MI:SS.FF') as timestamp5_with_ltz_utc,
      |        to_char(TIMESTAMP8_WITH_LTZ) as timestamp8_with_ltz , to_char(sys_extract_utc(TIMESTAMP8_WITH_LTZ), 'YYYY-MM-DD HH24:MI:SS.FF') as timestamp8_with_ltz_utc
      |        from DBSRV.ORACLE_TIMESTAMPS
      |""".stripMargin)
  .saveAs("__source__")
  .transformSql(
    """
      |SELECT
      |        TO_TIMESTAMP(ingest_ts_utc) as ingest_ts_utc,
      |        TIMESTAMP_0 as timestamp_0,
      |        TIMESTAMP_5 as timestamp_5,
      |        TIMESTAMP_7 as timestamp_7,
      |        TIMESTAMP_9 as timestamp_9,
      |        TIMESTAMP0_WITH_TZ as timestamp0_with_tz,TIMESTAMP0_WITH_TZ_utc as timestamp0_with_tz_utc,
      |        TIMESTAMP5_WITH_TZ as timestamp5_with_tz,TIMESTAMP5_WITH_TZ_utc as timestamp5_with_tz_utc,
      |        TIMESTAMP8_WITH_TZ as timestamp8_with_tz,TIMESTAMP8_WITH_TZ_utc as timestamp8_with_tz_utc,
      |        TIMESTAMP0_WITH_LTZ as timestamp0_with_ltz,TIMESTAMP0_WITH_LTZ_utc as timestamp0_with_ltz_utc,
      |        TIMESTAMP5_WITH_LTZ as timestamp5_with_ltz,TIMESTAMP5_WITH_LTZ_utc as timestamp5_with_ltz_utc,
      |        TIMESTAMP8_WITH_LTZ as timestamp8_with_ltz,TIMESTAMP8_WITH_LTZ_utc as timestamp8_with_ltz_utc
      |        from __source__
      |""".stripMargin)
  .targetJDBC(tableName = "pgdb.ora_to_postgres", properties, SaveMode.Overwrite)

oracleTOPostgresConnector.stop()

```

### Output

```commandline
21/05/04 17:35:50 INFO targetjdbc.JDBCtoJDBC: Executing source sql query:
SELECT
        to_char(sys_extract_utc(systimestamp), 'YYYY-MM-DD HH24:MI:SS.FF') as ingest_ts_utc,
        to_char(TIMESTAMP_0, 'YYYY-MM-DD HH24:MI:SS.FF') as timestamp_0,
        to_char(TIMESTAMP_5, 'YYYY-MM-DD HH24:MI:SS.FF') as timestamp_5,
        to_char(TIMESTAMP_7, 'YYYY-MM-DD HH24:MI:SS.FF') as timestamp_7,
        to_char(TIMESTAMP_9, 'YYYY-MM-DD HH24:MI:SS.FF') as timestamp_9,
        to_char(TIMESTAMP0_WITH_TZ) as timestamp0_with_tz , to_char(sys_extract_utc(TIMESTAMP0_WITH_TZ), 'YYYY-MM-DD HH24:MI:SS') as timestamp0_with_tz_utc,
        to_char(TIMESTAMP5_WITH_TZ) as timestamp5_with_tz , to_char(sys_extract_utc(TIMESTAMP5_WITH_TZ), 'YYYY-MM-DD HH24:MI:SS.FF') as timestamp5_with_tz_utc,
        to_char(TIMESTAMP8_WITH_TZ) as timestamp8_with_tz , to_char(sys_extract_utc(TIMESTAMP8_WITH_TZ), 'YYYY-MM-DD HH24:MI:SS.FF') as timestamp8_with_tz_utc
        from DBSRV.ORACLE_TIMESTAMPS

21/05/04 17:35:50 INFO targetjdbc.JDBCtoJDBC: Data is saved as a temporary table by name: __source__
21/05/04 17:35:50 INFO targetjdbc.JDBCtoJDBC: showing saved data from temporary table with name: __source__
+--------------------------+---------------------+-------------------------+---------------------------+-----------------------------+-----------------------------------+----------------------+-----------------------------------------+-------------------------+--------------------------------------------+----------------------------+
|INGEST_TS_UTC             |TIMESTAMP_0          |TIMESTAMP_5              |TIMESTAMP_7                |TIMESTAMP_9                  |TIMESTAMP0_WITH_TZ                 |TIMESTAMP0_WITH_TZ_UTC|TIMESTAMP5_WITH_TZ                       |TIMESTAMP5_WITH_TZ_UTC   |TIMESTAMP8_WITH_TZ                          |TIMESTAMP8_WITH_TZ_UTC      |
+--------------------------+---------------------+-------------------------+---------------------------+-----------------------------+-----------------------------------+----------------------+-----------------------------------------+-------------------------+--------------------------------------------+----------------------------+
|2021-05-04 17:35:50.620944|2021-04-07 15:15:16.0|2021-04-07 15:15:16.03356|2021-04-07 15:15:16.0335610|2021-04-07 15:15:16.033561000|07-APR-21 03.15.16 PM ASIA/CALCUTTA|2021-04-07 09:45:16   |07-APR-21 03.15.16.03356 PM ASIA/CALCUTTA|2021-04-07 09:45:16.03356|07-APR-21 03.15.16.03356100 PM ASIA/CALCUTTA|2021-04-07 09:45:16.03356100|
|2021-05-04 17:35:50.620944|2021-04-07 15:16:51.6|2021-04-07 15:16:51.60911|2021-04-07 15:16:51.6091090|2021-04-07 15:16:51.609109000|07-APR-21 03.16.52 PM ASIA/CALCUTTA|2021-04-07 09:46:52   |07-APR-21 03.16.51.60911 PM ASIA/CALCUTTA|2021-04-07 09:46:51.60911|07-APR-21 03.16.51.60910900 PM ASIA/CALCUTTA|2021-04-07 09:46:51.60910900|
+--------------------------+---------------------+-------------------------+---------------------------+-----------------------------+-----------------------------------+----------------------+-----------------------------------------+-------------------------+--------------------------------------------+----------------------------+

21/05/04 17:35:50 INFO targetjdbc.JDBCtoJDBC: Data after transformation using the SQL :
SELECT
        TO_TIMESTAMP(ingest_ts_utc) as ingest_ts_utc,
        TIMESTAMP_0 as timestamp_0,
        TIMESTAMP_5 as timestamp_5,
        TIMESTAMP_7 as timestamp_7,
        TIMESTAMP_9 as timestamp_9,
        TIMESTAMP0_WITH_TZ as timestamp0_with_tz,TIMESTAMP0_WITH_TZ_utc as timestamp0_with_tz_utc,
        TIMESTAMP5_WITH_TZ as timestamp5_with_tz,TIMESTAMP5_WITH_TZ_utc as timestamp5_with_tz_utc,
        TIMESTAMP8_WITH_TZ as timestamp8_with_tz,TIMESTAMP8_WITH_TZ_utc as timestamp8_with_tz_utc
        from __source__

+--------------------------+---------------------+-------------------------+---------------------------+-----------------------------+-----------------------------------+----------------------+-----------------------------------------+-------------------------+--------------------------------------------+----------------------------+
|ingest_ts_utc             |timestamp_0          |timestamp_5              |timestamp_7                |timestamp_9                  |timestamp0_with_tz                 |timestamp0_with_tz_utc|timestamp5_with_tz                       |timestamp5_with_tz_utc   |timestamp8_with_tz                          |timestamp8_with_tz_utc      |
+--------------------------+---------------------+-------------------------+---------------------------+-----------------------------+-----------------------------------+----------------------+-----------------------------------------+-------------------------+--------------------------------------------+----------------------------+
|2021-05-04 17:35:50.818643|2021-04-07 15:15:16.0|2021-04-07 15:15:16.03356|2021-04-07 15:15:16.0335610|2021-04-07 15:15:16.033561000|07-APR-21 03.15.16 PM ASIA/CALCUTTA|2021-04-07 09:45:16   |07-APR-21 03.15.16.03356 PM ASIA/CALCUTTA|2021-04-07 09:45:16.03356|07-APR-21 03.15.16.03356100 PM ASIA/CALCUTTA|2021-04-07 09:45:16.03356100|
|2021-05-04 17:35:50.818643|2021-04-07 15:16:51.6|2021-04-07 15:16:51.60911|2021-04-07 15:16:51.6091090|2021-04-07 15:16:51.609109000|07-APR-21 03.16.52 PM ASIA/CALCUTTA|2021-04-07 09:46:52   |07-APR-21 03.16.51.60911 PM ASIA/CALCUTTA|2021-04-07 09:46:51.60911|07-APR-21 03.16.51.60910900 PM ASIA/CALCUTTA|2021-04-07 09:46:51.60910900|
+--------------------------+---------------------+-------------------------+---------------------------+-----------------------------+-----------------------------------+----------------------+-----------------------------------------+-------------------------+--------------------------------------------+----------------------------+

21/05/04 17:35:50 INFO targetjdbc.JDBCtoJDBC: Writing data to target table: pgdb.ora_to_postgres
21/05/04 17:35:56 INFO targetjdbc.JDBCtoJDBC: Showing data in target table  : pgdb.ora_to_postgres
+--------------------------+---------------------+-------------------------+---------------------------+-----------------------------+-----------------------------------+----------------------+-----------------------------------------+-------------------------+--------------------------------------------+----------------------------+
|ingest_ts_utc             |timestamp_0          |timestamp_5              |timestamp_7                |timestamp_9                  |timestamp0_with_tz                 |timestamp0_with_tz_utc|timestamp5_with_tz                       |timestamp5_with_tz_utc   |timestamp8_with_tz                          |timestamp8_with_tz_utc      |
+--------------------------+---------------------+-------------------------+---------------------------+-----------------------------+-----------------------------------+----------------------+-----------------------------------------+-------------------------+--------------------------------------------+----------------------------+
|2021-05-04 17:35:52.709503|2021-04-07 15:15:16.0|2021-04-07 15:15:16.03356|2021-04-07 15:15:16.0335610|2021-04-07 15:15:16.033561000|07-APR-21 03.15.16 PM ASIA/CALCUTTA|2021-04-07 09:45:16   |07-APR-21 03.15.16.03356 PM ASIA/CALCUTTA|2021-04-07 09:45:16.03356|07-APR-21 03.15.16.03356100 PM ASIA/CALCUTTA|2021-04-07 09:45:16.03356100|
|2021-05-04 17:35:52.709503|2021-04-07 15:16:51.6|2021-04-07 15:16:51.60911|2021-04-07 15:16:51.6091090|2021-04-07 15:16:51.609109000|07-APR-21 03.16.52 PM ASIA/CALCUTTA|2021-04-07 09:46:52   |07-APR-21 03.16.51.60911 PM ASIA/CALCUTTA|2021-04-07 09:46:51.60911|07-APR-21 03.16.51.60910900 PM ASIA/CALCUTTA|2021-04-07 09:46:51.60910900|
+--------------------------+---------------------+-------------------------+---------------------------+-----------------------------+-----------------------------------+----------------------+-----------------------------------------+-------------------------+--------------------------------------------+----------------------------+
```

#### Postgres to Salesforce Connector
While writing to Salesforce,below are the pre-requisites one must take care of:
1. A salesforce object/dataset must be available at Salesforce as spark will not create an object automatically.
2. Also the columns/fields in source data or dataframe must match the fileds in the destination salesforce object

```scala
import com.github.edge.roman.spear.SpearConnector
import org.apache.spark.sql.{Column, DataFrame, SaveMode}
import java.util.Properties

val targetproperties = new Properties()
  targetproperties.put("username", "user")
  targetproperties.put("password", "pass<TOKEN>")

val postgresSalesForce = SpearConnector
    .createConnector("POSTGRES-SALESFORCE")
    .source(sourceType = "relational", sourceFormat = "jdbc")
    .target(targetType = "relational", targetFormat = "soql")
    .getConnector

postgresSalesForce
    .source(sourceObject = "salesforce_source", Map("driver" -> "org.postgresql.Driver", "user" -> "postgres_user", "password" -> "mysecretpassword", "url" -> "jdbc:postgresql://postgres:5432/pgdb"))
    .saveAs("__tmp__")
    .transformSql(
      """
        |select person_id as person_id__c,
        |name as person_name__c
        |from __tmp__""".stripMargin)
    .targetJDBC(tableName = "Sample__c", targetproperties, SaveMode.Overwrite)
postgresSalesForce.stop()
```

### Streaming source

#### Kafka to Postgres Connector

```scala
import com.github.edge.roman.spear.SpearConnector
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import java.util.Properties

val properties = new Properties()
properties.put("driver", "org.postgresql.Driver");
properties.put("user", "postgres_user")
properties.put("password", "mysecretpassword")
properties.put("url", "jdbc:postgresql://postgres:5432/pgdb")

val streamTOPostgres=SpearConnector
   .createConnector(name="StreamKafkaToPostgresconnector")
   .source(sourceType = "stream",sourceFormat = "kafka")
   .target(targetType = "relational",targetFormat = "jdbc")
   .getConnector

val schema = StructType(
    Array(StructField("id", StringType),
      StructField("name", StringType)
    ))

streamTOPostgres
    .source(sourceObject = "stream_topic",Map("kafka.bootstrap.servers"-> "kafka:9092","failOnDataLoss"->"true","startingOffsets"-> "earliest"),schema)
    .saveAs("__tmp2__")
    .transformSql("select cast (id as INT) as id, name from __tmp2__")
    .targetJDBC(tableName="person", properties, SaveMode.Append)

streamTOPostgres.stop()
```

### Target FS (HDFS)

#### Postgres to Hive Connector

```scala
import com.github.edge.roman.spear.SpearConnector
import org.apache.spark.sql.SaveMode
import org.apache.log4j.{Level, Logger}
import java.util.Properties

Logger.getLogger("com.github").setLevel(Level.INFO)

val postgresToHiveConnector = SpearConnector
  .createConnector(name="PostgrestoHiveConnector")
  .source(sourceType = "relational", sourceFormat = "jdbc")
  .target(targetType = "FS", targetFormat = "parquet")
  .getConnector
  
postgresToHiveConnector.setVeboseLogging(true)  

postgresToHiveConnector
  .source("source_db.instance", Map("driver" -> "org.postgresql.Driver", "user" -> "postgres", "password" -> "test", "url" -> "jdbc:postgresql://postgres-host:5433/source_db"))
  .saveAs("__tmp__")
  .transformSql(
    """
      |select cast( uuid as string) as uuid,
      |cast( type_id as bigint ) as type_id, 
      |cast( factory_message_process_id as bigint) as factory_message_process_id,
      |cast( factory_uuid as string ) as factory_uuid,
      |cast( factory_id as bigint ) as factory_id,
      |cast( engine_id as bigint ) as engine_id,
      |cast( topic as string ) as topic,
      |cast( status_code_id as int) as status_code_id,
      |cast( cru_by as string ) as cru_by,cast( cru_ts as timestamp) as cru_ts 
      |from __tmp__""".stripMargin)
  .targetFS(destinationFilePath = "/tmp/ingest_test.db", saveAsTable = "ingest_test.postgres_data", SaveMode.Overwrite)

postgresToHiveConnector.stop()
```

### Output

```commandline
21/05/01 10:39:20 INFO targetFS.JDBCtoFS: Reading source data from table: source_db.instance
+------------------------------------+-----------+------------------------------+------------------------------------+--------------+------------------+---------------------------+--------------+------+--------------------------+
|uuid                                 |type_id    |factory_message_process_id   |factory_uuid                        |factory_id    |   engine_id      |topic                      |status_code_id|cru_by|cru_ts                    |
+------------------------------------+-----------+------------------------------+------------------------------------+--------------+------------------+---------------------------+--------------+------+--------------------------+
|null                                |1          |1619518657679                 |b218b4a2-2723-4a51-a83b-1d9e5e1c79ff|2             |2                 |factory_2_2                |5             |ABCDE |2021-04-27 10:17:37.529195|
|null                                |1          |1619518657481                 |ec65395c-fdbc-4697-ac91-bc72447ae7cf|1             |1                 |factory_1_1                |5             |ABCDE |2021-04-27 10:17:37.533318|
|null                                |1          |1619518658043                 |5ef4bcb3-f064-4532-ad4f-5e8b68c33f70|3             |3                 |factory_3_3                |5             |ABCDE |2021-04-27 10:17:37.535323|
|59d9b23e-ff93-4351-af7e-0a95ec4fde65|10         |1619518657481                 |ec65395c-fdbc-4697-ac91-bc72447ae7cf|1             |1                 |bale_1_authtoken           |5             |ABCDE |2021-04-27 10:17:50.441147|
|111eeff6-c61d-402e-9e70-615cf80d3016|10         |1619518657679                 |b218b4a2-2723-4a51-a83b-1d9e5e1c79ff|2             |2                 |bale_2_authtoken           |5             |ABCDE |2021-04-27 10:18:02.439379|
|2870ff43-73c9-424e-9f3c-c89ac4dda278|10         |1619518658043                 |5ef4bcb3-f064-4532-ad4f-5e8b68c33f70|3             |3                 |bale_3_authtoken           |5             |ABCDE |2021-04-27 10:18:14.5242  |
|58fe7575-9c4f-471e-8893-9bc39b4f1be4|18         |1619518658043                 |5ef4bcb3-f064-4532-ad4f-5e8b68c33f70|3             |3                 |bale_3_errorbot            |5             |ABCDE |2021-04-27 10:21:17.098984|
|534a2af0-af74-4633-8603-926070afd76f|16         |1619518657679                 |b218b4a2-2723-4a51-a83b-1d9e5e1c79ff|2             |2                 |bale_2_filter_resolver_jdbc|5             |ABCDE |2021-04-27 10:21:17.223042|
|9971130b-9ae1-4a53-89ce-aa1932534956|18         |1619518657481                 |ec65395c-fdbc-4697-ac91-bc72447ae7cf|1             |1                 |bale_1_errorbot            |5             |ABCDE |2021-04-27 10:21:17.437489|
|6db9c72f-85b0-4254-bc2f-09dc1e63e6f3|9          |1619518657481                 |ec65395c-fdbc-4697-ac91-bc72447ae7cf|1             |1                 |bale_1_flowcontroller      |5             |ABCDE |2021-04-27 10:21:17.780313|
+------------------------------------+-----------+------------------------------+------------------------------------+--------------+------------------+---------------------------+--------------+------+--------------------------+
only showing top 10 rows

21/05/01 10:39:31 INFO targetFS.JDBCtoFS: Data is saved as a temporary table by name: __tmp__
21/05/01 10:39:31 INFO targetFS.JDBCtoFS: showing saved data from temporary table with name: __tmp__
+------------------------------------+-----------+------------------------------+------------------------------------+--------------+------------------+---------------------------+--------------+------+--------------------------+
|uuid                                |type_id    |factory_message_process_id    |factory_uuid                        |factory_id    | engine_id        |topic                      |status_code_id|cru_by|cru_ts                    |
+------------------------------------+-----------+------------------------------+------------------------------------+--------------+------------------+---------------------------+--------------+------+--------------------------+
|null                                |1          |1619518657679                 |b218b4a2-2723-4a51-a83b-1d9e5e1c79ff|2             |2                 |factory_2_2                |5             |ABCDE |2021-04-27 10:17:37.529195|
|null                                |1          |1619518657481                 |ec65395c-fdbc-4697-ac91-bc72447ae7cf|1             |1                 |factory_1_1                |5             |ABCDE |2021-04-27 10:17:37.533318|
|null                                |1          |1619518658043                 |5ef4bcb3-f064-4532-ad4f-5e8b68c33f70|3             |3                 |factory_3_3                |5             |ABCDE |2021-04-27 10:17:37.535323|
|59d9b23e-ff93-4351-af7e-0a95ec4fde65|10         |1619518657481                 |ec65395c-fdbc-4697-ac91-bc72447ae7cf|1             |1                 |bale_1_authtoken           |5             |ABCDE |2021-04-27 10:17:50.441147|
|111eeff6-c61d-402e-9e70-615cf80d3016|10         |1619518657679                 |b218b4a2-2723-4a51-a83b-1d9e5e1c79ff|2             |2                 |bale_2_authtoken           |5             |ABCDE |2021-04-27 10:18:02.439379|
|2870ff43-73c9-424e-9f3c-c89ac4dda278|10         |1619518658043                 |5ef4bcb3-f064-4532-ad4f-5e8b68c33f70|3             |3                 |bale_3_authtoken           |5             |ABCDE |2021-04-27 10:18:14.5242  |
|58fe7575-9c4f-471e-8893-9bc39b4f1be4|18         |1619518658043                 |5ef4bcb3-f064-4532-ad4f-5e8b68c33f70|3             |3                 |bale_3_errorbot            |5             |ABCDE |2021-04-27 10:21:17.098984|
|534a2af0-af74-4633-8603-926070afd76f|16         |1619518657679                 |b218b4a2-2723-4a51-a83b-1d9e5e1c79ff|2             |2                 |bale_2_filter_resolver_jdbc|5             |ABCDE |2021-04-27 10:21:17.223042|
|9971130b-9ae1-4a53-89ce-aa1932534956|18         |1619518657481                 |ec65395c-fdbc-4697-ac91-bc72447ae7cf|1             |1                 |bale_1_errorbot            |5             |ABCDE |2021-04-27 10:21:17.437489|
|6db9c72f-85b0-4254-bc2f-09dc1e63e6f3|9          |1619518657481                 |ec65395c-fdbc-4697-ac91-bc72447ae7cf|1             |1                 |bale_1_flowcontroller      |5             |ABCDE |2021-04-27 10:21:17.780313|
+------------------------------------+-----------+------------------------------+------------------------------------+--------------+------------------+---------------------------+--------------+------+--------------------------+
only showing top 10 rows

21/05/01 10:39:33 INFO targetFS.JDBCtoFS: Data after transformation using the SQL :
select cast( uuid as string) as uuid,
cast( type_id as bigint ) as type_id,
cast( factory_message_process_id as bigint) as factory_message_process_id,
cast( factory_uuid as string ) as factory_uuid,
cast( factory_id as bigint ) as factory_id,
cast( workflow_engine_id as bigint ) as workflow_engine_id,
cast( topic as string ) as topic,
cast( status_code_id as int) as status_code_id,
cast( cru_by as string ) as cru_by,cast( cru_ts as timestamp) as cru_ts
from __tmp__
+------------------------------------+-----------+------------------------------+------------------------------------+--------------+------------------+---------------------------+--------------+------+--------------------------+
|uuid                                |type_id|factory_message_process_id        |factory_uuid                        |factory_id    |engine_id         |topic                      |status_code_id|cru_by|cru_ts                    |
+------------------------------------+-----------+------------------------------+------------------------------------+--------------+------------------+---------------------------+--------------+------+--------------------------+
|null                                |1          |1619518657679                 |b218b4a2-2723-4a51-a83b-1d9e5e1c79ff|2             |2                 |factory_2_2                |5             |ABCDE |2021-04-27 10:17:37.529195|
|null                                |1          |1619518657481                 |ec65395c-fdbc-4697-ac91-bc72447ae7cf|1             |1                 |factory_1_1                |5             |ABCDE |2021-04-27 10:17:37.533318|
|null                                |1          |1619518658043                 |5ef4bcb3-f064-4532-ad4f-5e8b68c33f70|3             |3                 |factory_3_3                |5             |ABCDE |2021-04-27 10:17:37.535323|
|59d9b23e-ff93-4351-af7e-0a95ec4fde65|10         |1619518657481                 |ec65395c-fdbc-4697-ac91-bc72447ae7cf|1             |1                 |bale_1_authtoken           |5             |ABCDE |2021-04-27 10:17:50.441147|
|111eeff6-c61d-402e-9e70-615cf80d3016|10         |1619518657679                 |b218b4a2-2723-4a51-a83b-1d9e5e1c79ff|2             |2                 |bale_2_authtoken           |5             |ABCDE |2021-04-27 10:18:02.439379|
|2870ff43-73c9-424e-9f3c-c89ac4dda278|10         |1619518658043                 |5ef4bcb3-f064-4532-ad4f-5e8b68c33f70|3             |3                 |bale_3_authtoken           |5             |ABCDE |2021-04-27 10:18:14.5242  |
|58fe7575-9c4f-471e-8893-9bc39b4f1be4|18         |1619518658043                 |5ef4bcb3-f064-4532-ad4f-5e8b68c33f70|3             |3                 |bale_3_errorbot            |5             |ABCDE |2021-04-27 10:21:17.098984|
|534a2af0-af74-4633-8603-926070afd76f|16         |1619518657679                 |b218b4a2-2723-4a51-a83b-1d9e5e1c79ff|2             |2                 |bale_2_filter_resolver_jdbc|5             |ABCDE |2021-04-27 10:21:17.223042|
|9971130b-9ae1-4a53-89ce-aa1932534956|18         |1619518657481                 |ec65395c-fdbc-4697-ac91-bc72447ae7cf|1             |1                 |bale_1_errorbot            |5             |ABCDE |2021-04-27 10:21:17.437489|
|6db9c72f-85b0-4254-bc2f-09dc1e63e6f3|9          |1619518657481                 |ec65395c-fdbc-4697-ac91-bc72447ae7cf|1             |1                 |bale_1_flowcontroller      |5             |ABCDE |2021-04-27 10:21:17.780313|
+------------------------------------+-----------+------------------------------+------------------------------------+--------------+------------------+---------------------------+--------------+------+--------------------------+
only showing top 10 rows

21/05/01 10:39:35 INFO targetFS.JDBCtoFS: Writing data to target file: /tmp/ingest_test.db
21/05/01 10:39:35 INFO targetFS.JDBCtoFS: Saving data to table:ingest_test.postgres_data
21/05/01 10:39:35 INFO targetFS.JDBCtoFS: Target Data in table:ingest_test.postgres_data

+------------------------------------+-----------+------------------------------+------------------------------------+--------------+------------------+---------------------------+--------------+------+--------------------------+
|uuid                                |type_id    |factory_message_process_id    |factory_uuid                        |factory_id    |        engine_id |topic                      |status_code_id|cru_by|cru_ts                    |
+------------------------------------+-----------+------------------------------+------------------------------------+--------------+------------------+---------------------------+--------------+------+--------------------------+
|null                                |1          |1619518657679                 |b218b4a2-2723-4a51-a83b-1d9e5e1c79ff|2             |2                 |factory_2_2                |5             |ABCDE |2021-04-27 10:17:37.529195|
|null                                |1          |1619518657481                 |ec65395c-fdbc-4697-ac91-bc72447ae7cf|1             |1                 |factory_1_1                |5             |ABCDE |2021-04-27 10:17:37.533318|
|null                                |1          |1619518658043                 |5ef4bcb3-f064-4532-ad4f-5e8b68c33f70|3             |3                 |factory_3_3                |5             |ABCDE |2021-04-27 10:17:37.535323|
|59d9b23e-ff93-4351-af7e-0a95ec4fde65|10         |1619518657481                 |ec65395c-fdbc-4697-ac91-bc72447ae7cf|1             |1                 |bale_1_authtoken           |5             |ABCDE |2021-04-27 10:17:50.441147|
|111eeff6-c61d-402e-9e70-615cf80d3016|10         |1619518657679                 |b218b4a2-2723-4a51-a83b-1d9e5e1c79ff|2             |2                 |bale_2_authtoken           |5             |ABCDE |2021-04-27 10:18:02.439379|
|2870ff43-73c9-424e-9f3c-c89ac4dda278|10         |1619518658043                 |5ef4bcb3-f064-4532-ad4f-5e8b68c33f70|3             |3                 |bale_3_authtoken           |5             |ABCDE |2021-04-27 10:18:14.5242  |
|58fe7575-9c4f-471e-8893-9bc39b4f1be4|18         |1619518658043                 |5ef4bcb3-f064-4532-ad4f-5e8b68c33f70|3             |3                 |bale_3_errorbot            |5             |ABCDE |2021-04-27 10:21:17.098984|
|534a2af0-af74-4633-8603-926070afd76f|16         |1619518657679                 |b218b4a2-2723-4a51-a83b-1d9e5e1c79ff|2             |2                 |bale_2_filter_resolver_jdbc|5             |ABCDE |2021-04-27 10:21:17.223042|
|9971130b-9ae1-4a53-89ce-aa1932534956|18         |1619518657481                 |ec65395c-fdbc-4697-ac91-bc72447ae7cf|1             |1                 |bale_1_errorbot            |5             |ABCDE |2021-04-27 10:21:17.437489|
|6db9c72f-85b0-4254-bc2f-09dc1e63e6f3|9          |1619518657481                 |ec65395c-fdbc-4697-ac91-bc72447ae7cf|1             |1                 |bale_1_flowcontroller      |5             |ABCDE |2021-04-27 10:21:17.780313|
+------------------------------------+-----------+------------------------------+------------------------------------+--------------+------------------+---------------------------+--------------+------+--------------------------+
only showing top 10 rows
```

#### Oracle to Hive Connector

```scala
import com.github.edge.roman.spear.SpearConnector
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SaveMode

Logger.getLogger("com.github").setLevel(Level.INFO)

val oraToHiveConnector = SpearConnector.init
  .source(sourceType = "relational", sourceFormat = "jdbc")
  .target(targetType = "FS", targetFormat = "parquet")
  .getConnector

oraToHiveConnector.setVeboseLogging(true)
oraToHiveConnector
.sourceSql(Map("driver" -> "oracle.jdbc.driver.OracleDriver", "user" -> "user", "password" -> "pass", "url" -> "jdbc:oracle:thin:@ora-host:1521:orcl"),
  """
    |SELECT
    |        to_char(sys_extract_utc(systimestamp), 'YYYY-MM-DD HH24:MI:SS.FF') as ingest_ts_utc,
    |        cast(NUMBER_TYPE as varchar(255)) as number_type,
    |        cast(TINYINT_TYPE_1 as varchar(255)) as tinyint_type_1,
    |        cast(TINYINT_TYPE_2 as varchar(255)) as tinyint_type_2,
    |        cast(INT_TYPE_5 as varchar(255)) as int_type_5,
    |        cast(INT_TYPE_9 as varchar(255)) as int_type_9,
    |        cast(BIGINT_TYPE_15 as varchar(255)) as bigint_type_15,
    |        cast(BIGINT_TYPE_18 as varchar(255)) as bigint_type_18,
    |        cast(NUMBER_TYPE_31 as varchar(255)) as number_type_31,
    |        cast(NUMBER_TYPE_38 as varchar(255)) as number_type_38,
    |        cast(NUMBER_TYPE_7_4 as varchar(255)) as number_type_7_4,
    |        cast(NUMBER_TYPE_13_7 as varchar(255)) as number_type_13_7
    |        from DBSRV.ORACLE_NUMBER
    |""".stripMargin
).saveAs("__TF_SOURCE_TABLE__")
  .transformSql(
    """
      |SELECT
      |        TO_TIMESTAMP(ingest_ts_utc) as ingest_ts_utc,
      |        NUMBER_TYPE as number_type,
      |        cast(TINYINT_TYPE_1 as TINYINT) as tinyint_type_1,
      |        cast(TINYINT_TYPE_2 as TINYINT) as tinyint_type_2,
      |        cast(INT_TYPE_5 as INT) as int_type_5,
      |        cast(INT_TYPE_9 as INT) as int_type_9,
      |        cast(BIGINT_TYPE_15 as BIGINT) as bigint_type_15,
      |        cast(BIGINT_TYPE_18 as BIGINT) as bigint_type_18,
      |        cast(NUMBER_TYPE_31 as DECIMAL(31,0)) as number_type_31,
      |        cast(NUMBER_TYPE_38 as DECIMAL(38,0)) as number_type_38,
      |        cast(NUMBER_TYPE_7_4 as DECIMAL(7,4)) as number_type_7_4,
      |        cast(NUMBER_TYPE_13_7 as DECIMAL(13,7)) as number_type_13_7
      |        from __TF_SOURCE_TABLE__
      |""".stripMargin)
  .targetFS(destinationFilePath = "/tmp/ingest_test.db", saveAsTable = "ingest_test.ora_data", SaveMode.Overwrite)
oraToHiveConnector.stop()

```

### Output

```commandline
21/05/03 17:19:46 INFO targetFS.JDBCtoFS: Executing source sql query:
SELECT
        to_char(sys_extract_utc(systimestamp), 'YYYY-MM-DD HH24:MI:SS.FF') as ingest_ts_utc,
        cast(NUMBER_TYPE as varchar(255)) as number_type,
        cast(TINYINT_TYPE_1 as varchar(255)) as tinyint_type_1,
        cast(TINYINT_TYPE_2 as varchar(255)) as tinyint_type_2,
        cast(INT_TYPE_5 as varchar(255)) as int_type_5,
        cast(INT_TYPE_9 as varchar(255)) as int_type_9,
        cast(BIGINT_TYPE_15 as varchar(255)) as bigint_type_15,
        cast(BIGINT_TYPE_18 as varchar(255)) as bigint_type_18,
        cast(NUMBER_TYPE_31 as varchar(255)) as number_type_31,
        cast(NUMBER_TYPE_38 as varchar(255)) as number_type_38,
        cast(NUMBER_TYPE_7_4 as varchar(255)) as number_type_7_4,
        cast(NUMBER_TYPE_13_7 as varchar(255)) as number_type_13_7
        from DBSRV.ORACLE_NUMBER

21/05/03 17:19:46 INFO targetFS.JDBCtoFS: Data is saved as a temporary table by name: __TF_SOURCE_TABLE__
21/05/03 17:19:46 INFO targetFS.JDBCtoFS: showing saved data from temporary table with name: __TF_SOURCE_TABLE__
+--------------------------+------------------+--------------+--------------+----------+----------+---------------+------------------+---------------------------+-------------------------------+---------------+----------------+
|INGEST_TS_UTC             |NUMBER_TYPE       |TINYINT_TYPE_1|TINYINT_TYPE_2|INT_TYPE_5|INT_TYPE_9|BIGINT_TYPE_15 |BIGINT_TYPE_18    |NUMBER_TYPE_31             |NUMBER_TYPE_38                 |NUMBER_TYPE_7_4|NUMBER_TYPE_13_7|
+--------------------------+------------------+--------------+--------------+----------+----------+---------------+------------------+---------------------------+-------------------------------+---------------+----------------+
|2021-05-03 17:19:46.664410|1244124343.2341111|5             |89            |98984     |788288722 |788288722989848|788288722989848897|788288722989848897788288722|7882887229898488977882887228987|322.1311       |132431.2144     |
|2021-05-03 17:19:46.664410|78441243.2341111  |9             |89            |98984     |7888722   |788722989848   |288722989848897   |7882887229898488288722     |78828872288977882887228987     |322.1311       |132431.2144     |
+--------------------------+------------------+--------------+--------------+----------+----------+---------------+------------------+---------------------------+-------------------------------+---------------+----------------+

21/05/03 17:19:46 INFO targetFS.JDBCtoFS: Data after transformation using the SQL :
SELECT
        TO_TIMESTAMP(ingest_ts_utc) as ingest_ts_utc,
        NUMBER_TYPE as number_type,
        cast(TINYINT_TYPE_1 as TINYINT) as tinyint_type_1,
        cast(TINYINT_TYPE_2 as TINYINT) as tinyint_type_2,
        cast(INT_TYPE_5 as INT) as int_type_5,
        cast(INT_TYPE_9 as INT) as int_type_9,
        cast(BIGINT_TYPE_15 as BIGINT) as bigint_type_15,
        cast(BIGINT_TYPE_18 as BIGINT) as bigint_type_18,
        cast(NUMBER_TYPE_31 as DECIMAL(31,0)) as number_type_31,
        cast(NUMBER_TYPE_38 as DECIMAL(38,0)) as number_type_38,
        cast(NUMBER_TYPE_7_4 as DECIMAL(7,4)) as number_type_7_4,
        cast(NUMBER_TYPE_13_7 as DECIMAL(13,7)) as number_type_13_7
        from __TF_SOURCE_TABLE__

+--------------------------+------------------+--------------+--------------+----------+----------+---------------+------------------+---------------------------+-------------------------------+---------------+----------------+
|ingest_ts_utc             |number_type       |tinyint_type_1|tinyint_type_2|int_type_5|int_type_9|bigint_type_15 |bigint_type_18    |number_type_31             |number_type_38                 |number_type_7_4|number_type_13_7|
+--------------------------+------------------+--------------+--------------+----------+----------+---------------+------------------+---------------------------+-------------------------------+---------------+----------------+
|2021-05-03 17:19:46.973852|1244124343.2341111|5             |89            |98984     |788288722 |788288722989848|788288722989848897|788288722989848897788288722|7882887229898488977882887228987|322.1311       |132431.2144000  |
|2021-05-03 17:19:46.973852|78441243.2341111  |9             |89            |98984     |7888722   |788722989848   |288722989848897   |7882887229898488288722     |78828872288977882887228987     |322.1311       |132431.2144000  |
+--------------------------+------------------+--------------+--------------+----------+----------+---------------+------------------+---------------------------+-------------------------------+---------------+----------------+

21/05/03 17:19:46 INFO targetFS.JDBCtoFS: Writing data to target file: /tmp/ingest_test.db
21/05/03 17:19:53 INFO targetFS.JDBCtoFS: Saving data to table:ingest_test.ora_data
21/05/03 17:19:53 INFO targetFS.JDBCtoFS: Target Data in table:ingest_test.ora_data

+--------------------------+------------------+--------------+--------------+----------+----------+---------------+------------------+---------------------------+-------------------------------+---------------+----------------+
|ingest_ts_utc             |number_type       |tinyint_type_1|tinyint_type_2|int_type_5|int_type_9|bigint_type_15 |bigint_type_18    |number_type_31             |number_type_38                 |number_type_7_4|number_type_13_7|
+--------------------------+------------------+--------------+--------------+----------+----------+---------------+------------------+---------------------------+-------------------------------+---------------+----------------+
|2021-05-03 17:19:47.533503|1244124343.2341111|5             |89            |98984     |788288722 |788288722989848|788288722989848897|788288722989848897788288722|7882887229898488977882887228987|322.1311       |132431.2144000  |
|2021-05-03 17:19:47.533503|78441243.2341111  |9             |89            |98984     |7888722   |788722989848   |288722989848897   |7882887229898488288722     |78828872288977882887228987     |322.1311       |132431.2144000  |
+--------------------------+------------------+--------------+--------------+----------+----------+---------------+------------------+---------------------------+-------------------------------+---------------+----------------+

```

#### Salesforce to Hive Connector
Spark cannot read a salesforce object directly if specified,You must always use a soql or a saql query to read from the Salesforce object.

```scala

  import com.github.edge.roman.spear.SpearConnector
  import org.apache.spark.sql.{Column, DataFrame, SaveMode}
  val salseforceToHiveConnector = SpearConnector
    .createConnector("salseforce-hive")
    .source(sourceType = "relational", sourceFormat = "soql")
    .target(targetType = "FS", targetFormat = "parquet")
    .getConnector

  salseforceToHiveConnector.sourceSql(Map( "login"-> "https://login.salesforce.com","username" -> "user", "password" -> "pass"),
    """select
		  |    Id,
		  |    name,
		  |    OwnerId,
		  |    age__c,
		  |    gender__c,
		  |    guardianname__c,
		  |    ingest_ts_utc__c,
		  |    name_siml_score__c,
		  |    year_of_birth__c,
		  |    LastModifiedById,
		  |    LastModifiedDate
		  |    from sales_test__c""".stripMargin).saveAs("__temp__")
    .transformSql("""select
      |    Id,
      |    name,
      |    OwnerId,
      |    age__c,
      |    gender__c,
      |    guardianname__c,
      |    ingest_ts_utc__c,
      |    cast(name_siml_score__c as Float) name_siml_score__c,
      |    year_of_birth__c,
      |    LastModifiedById,
      |    cast(unix_timestamp(LastModifiedDate,"yyyy-MM-dd") AS timestamp) as LastModifiedDate
      |    from __temp__""".stripMargin)
    .targetFS("/user/hive/metastore/salseforce.db",saveAsTable="salseforce.transform_hive",SaveMode.Overwrite)
    
    salseforceToHiveConnector.stop()
```

### Streaming source

#### Kafka to Hive Connector

```scala
import com.github.edge.roman.spear.SpearConnector
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

val streamTOHdfs=SpearConnector
  .createConnector(name="StreamKafkaToPostgresconnector")
  .source(sourceType = "stream",sourceFormat = "kafka")
  .target(targetType = "FS",targetFormat = "parquet")
  .getConnector

val schema = StructType(
  Array(StructField("id", StringType),
    StructField("name", StringType)
  ))

streamTOHdfs
  .source(sourceObject = "stream_topic",Map("kafka.bootstrap.servers"-> "kafka:9092","failOnDataLoss"->"true","startingOffsets"-> "earliest"),schema)
  .saveAs("__tmp2__")
  .transformSql("select cast (id as INT), name as __tmp2__")
  .targetFS(destinationFilePath = "/tmp/ingest_test.db", saveAsTable = "ingest_test.ora_data", SaveMode.Append)

streamTOHdfs.stop()
```



### Target FS (Cloud)

#### Oracle To S3 Connector

```scala
import com.github.edge.roman.spear.SpearConnector
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SaveMode

Logger.getLogger("com.github").setLevel(Level.INFO)

spark.sparkContext.hadoopConfiguration.set("fs.s3a.access.key", "*****")
spark.sparkContext.hadoopConfiguration.set("fs.s3a.secret.key", "*****")


val oracleTOS3Connector = SpearConnector.init
  .source(sourceType = "relational", sourceFormat = "jdbc")
  .target(targetType = "FS", targetFormat = "parquet")
  .withName(connectorName ="OracleToS3Connector" )
  .getConnector
oracleTOS3Connector.setVeboseLogging(true)  
oracleTOS3Connector
  .sourceSql(Map("driver" -> "oracle.jdbc.driver.OracleDriver", "user" -> "user", "password" -> "pass", "url" -> "jdbc:oracle:thin:@ora-host:1521:orcl"),
    """
      |SELECT
      |        to_char(sys_extract_utc(systimestamp), 'YYYY-MM-DD HH24:MI:SS.FF') as ingest_ts_utc,
      |        to_char(TIMESTAMP_0, 'YYYY-MM-DD HH24:MI:SS.FF') as timestamp_0,
      |        to_char(TIMESTAMP_5, 'YYYY-MM-DD HH24:MI:SS.FF') as timestamp_5,
      |        to_char(TIMESTAMP_7, 'YYYY-MM-DD HH24:MI:SS.FF') as timestamp_7,
      |        to_char(TIMESTAMP_9, 'YYYY-MM-DD HH24:MI:SS.FF') as timestamp_9,
      |        to_char(TIMESTAMP0_WITH_TZ) as timestamp0_with_tz , to_char(sys_extract_utc(TIMESTAMP0_WITH_TZ), 'YYYY-MM-DD HH24:MI:SS') as timestamp0_with_tz_utc,
      |        to_char(TIMESTAMP5_WITH_TZ) as timestamp5_with_tz , to_char(sys_extract_utc(TIMESTAMP5_WITH_TZ), 'YYYY-MM-DD HH24:MI:SS.FF') as timestamp5_with_tz_utc,
      |        to_char(TIMESTAMP8_WITH_TZ) as timestamp8_with_tz , to_char(sys_extract_utc(TIMESTAMP8_WITH_TZ), 'YYYY-MM-DD HH24:MI:SS.FF') as timestamp8_with_tz_utc
      |        from DBSRV.ORACLE_NUMBER
      |""".stripMargin)
  .saveAs("__source__")
  .transformSql(
    """
      |SELECT
      |        TO_TIMESTAMP(ingest_ts_utc) as ingest_ts_utc,
      |        TIMESTAMP_0 as timestamp_0,
      |        TIMESTAMP_5 as timestamp_5,
      |        TIMESTAMP_7 as timestamp_7,
      |        TIMESTAMP_9 as timestamp_9,
      |        TIMESTAMP0_WITH_TZ as timestamp0_with_tz,TIMESTAMP0_WITH_TZ_utc as timestamp0_with_tz_utc,
      |        TIMESTAMP5_WITH_TZ as timestamp5_with_tz,TIMESTAMP5_WITH_TZ_utc as timestamp5_with_tz_utc,
      |        TIMESTAMP8_WITH_TZ as timestamp8_with_tz,TIMESTAMP8_WITH_TZ_utc as timestamp8_with_tz_utc
      |        from __source__
      |""".stripMargin)
  .targetFS(destinationFilePath="s3a://destination/data",SaveMode.Overwrite)

oracleTOS3Connector.stop()
```

### Output

```commandline
21/05/08 08:46:11 INFO targetFS.JDBCtoFS: Executing source sql query:
SELECT
        to_char(sys_extract_utc(systimestamp), 'YYYY-MM-DD HH24:MI:SS.FF') as ingest_ts_utc,
        to_char(TIMESTAMP_0, 'YYYY-MM-DD HH24:MI:SS.FF') as timestamp_0,
        to_char(TIMESTAMP_5, 'YYYY-MM-DD HH24:MI:SS.FF') as timestamp_5,
        to_char(TIMESTAMP_7, 'YYYY-MM-DD HH24:MI:SS.FF') as timestamp_7,
        to_char(TIMESTAMP_9, 'YYYY-MM-DD HH24:MI:SS.FF') as timestamp_9,
        to_char(TIMESTAMP0_WITH_TZ) as timestamp0_with_tz , to_char(sys_extract_utc(TIMESTAMP0_WITH_TZ), 'YYYY-MM-DD HH24:MI:SS') as timestamp0_with_tz_utc,
        to_char(TIMESTAMP5_WITH_TZ) as timestamp5_with_tz , to_char(sys_extract_utc(TIMESTAMP5_WITH_TZ), 'YYYY-MM-DD HH24:MI:SS.FF') as timestamp5_with_tz_utc,
        to_char(TIMESTAMP8_WITH_TZ) as timestamp8_with_tz , to_char(sys_extract_utc(TIMESTAMP8_WITH_TZ), 'YYYY-MM-DD HH24:MI:SS.FF') as timestamp8_with_tz_utc
        from DBSRV.ORACLE_TIMESTAMPS

21/05/08 08:46:11 INFO targetFS.JDBCtoFS: Data is saved as a temporary table by name: __source__
21/05/08 08:46:11 INFO targetFS.JDBCtoFS: Showing saved data from temporary table with name: __source__
+--------------------------+---------------------+-------------------------+---------------------------+-----------------------------+-----------------------------------+----------------------+-----------------------------------------+-------------------------+--------------------------------------------+----------------------------+
|INGEST_TS_UTC             |TIMESTAMP_0          |TIMESTAMP_5              |TIMESTAMP_7                |TIMESTAMP_9                  |TIMESTAMP0_WITH_TZ                 |TIMESTAMP0_WITH_TZ_UTC|TIMESTAMP5_WITH_TZ                       |TIMESTAMP5_WITH_TZ_UTC   |TIMESTAMP8_WITH_TZ                          |TIMESTAMP8_WITH_TZ_UTC      |
+--------------------------+---------------------+-------------------------+---------------------------+-----------------------------+-----------------------------------+----------------------+-----------------------------------------+-------------------------+--------------------------------------------+----------------------------+
|2021-05-08 08:46:12.178719|2021-04-07 15:15:16.0|2021-04-07 15:15:16.03356|2021-04-07 15:15:16.0335610|2021-04-07 15:15:16.033561000|07-APR-21 03.15.16 PM ASIA/CALCUTTA|2021-04-07 09:45:16   |07-APR-21 03.15.16.03356 PM ASIA/CALCUTTA|2021-04-07 09:45:16.03356|07-APR-21 03.15.16.03356100 PM ASIA/CALCUTTA|2021-04-07 09:45:16.03356100|
|2021-05-08 08:46:12.178719|2021-04-07 15:16:51.6|2021-04-07 15:16:51.60911|2021-04-07 15:16:51.6091090|2021-04-07 15:16:51.609109000|07-APR-21 03.16.52 PM ASIA/CALCUTTA|2021-04-07 09:46:52   |07-APR-21 03.16.51.60911 PM ASIA/CALCUTTA|2021-04-07 09:46:51.60911|07-APR-21 03.16.51.60910900 PM ASIA/CALCUTTA|2021-04-07 09:46:51.60910900|
+--------------------------+---------------------+-------------------------+---------------------------+-----------------------------+-----------------------------------+----------------------+-----------------------------------------+-------------------------+--------------------------------------------+----------------------------+

21/05/08 08:46:12 INFO targetFS.JDBCtoFS: Data after transformation using the SQL :
SELECT
        TO_TIMESTAMP(ingest_ts_utc) as ingest_ts_utc,
        TIMESTAMP_0 as timestamp_0,
        TIMESTAMP_5 as timestamp_5,
        TIMESTAMP_7 as timestamp_7,
        TIMESTAMP_9 as timestamp_9,
        TIMESTAMP0_WITH_TZ as timestamp0_with_tz,TIMESTAMP0_WITH_TZ_utc as timestamp0_with_tz_utc,
        TIMESTAMP5_WITH_TZ as timestamp5_with_tz,TIMESTAMP5_WITH_TZ_utc as timestamp5_with_tz_utc,
        TIMESTAMP8_WITH_TZ as timestamp8_with_tz,TIMESTAMP8_WITH_TZ_utc as timestamp8_with_tz_utc
        from __source__

+--------------------------+---------------------+-------------------------+---------------------------+-----------------------------+-----------------------------------+----------------------+-----------------------------------------+-------------------------+--------------------------------------------+----------------------------+
|ingest_ts_utc             |timestamp_0          |timestamp_5              |timestamp_7                |timestamp_9                  |timestamp0_with_tz                 |timestamp0_with_tz_utc|timestamp5_with_tz                       |timestamp5_with_tz_utc   |timestamp8_with_tz                          |timestamp8_with_tz_utc      |
+--------------------------+---------------------+-------------------------+---------------------------+-----------------------------+-----------------------------------+----------------------+-----------------------------------------+-------------------------+--------------------------------------------+----------------------------+
|2021-05-08 08:46:12.438578|2021-04-07 15:15:16.0|2021-04-07 15:15:16.03356|2021-04-07 15:15:16.0335610|2021-04-07 15:15:16.033561000|07-APR-21 03.15.16 PM ASIA/CALCUTTA|2021-04-07 09:45:16   |07-APR-21 03.15.16.03356 PM ASIA/CALCUTTA|2021-04-07 09:45:16.03356|07-APR-21 03.15.16.03356100 PM ASIA/CALCUTTA|2021-04-07 09:45:16.03356100|
|2021-05-08 08:46:12.438578|2021-04-07 15:16:51.6|2021-04-07 15:16:51.60911|2021-04-07 15:16:51.6091090|2021-04-07 15:16:51.609109000|07-APR-21 03.16.52 PM ASIA/CALCUTTA|2021-04-07 09:46:52   |07-APR-21 03.16.51.60911 PM ASIA/CALCUTTA|2021-04-07 09:46:51.60911|07-APR-21 03.16.51.60910900 PM ASIA/CALCUTTA|2021-04-07 09:46:51.60910900|
+--------------------------+---------------------+-------------------------+---------------------------+-----------------------------+-----------------------------------+----------------------+-----------------------------------------+-------------------------+--------------------------------------------+----------------------------+
21/05/08 08:46:12 INFO targetFS.JDBCtoFS: Writing data to target path: s3a://destination/data
21/05/08 08:47:06 INFO targetFS.JDBCtoFS: Saving data to path:s3a://destination/data

Data at S3:
===========
user@node:~$ aws s3 ls s3://destination/data
2021-05-08 12:10:09          0 _SUCCESS
2021-05-08 12:09:59       4224 part-00000-71fad52e-404d-422c-a6af-7889691bc506-c000.snappy.parquet

```

#### Postgres To GCS Connector

```scala

import com.github.edge.roman.spear.SpearConnector
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SaveMode

spark.sparkContext.hadoopConfiguration.set("fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
spark.sparkContext.hadoopConfiguration.set("fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
spark.sparkContext.hadoopConfiguration.set("google.cloud.auth.service.account.json.keyfile","*********")

Logger.getLogger("com.github").setLevel(Level.INFO)
 val postgresToGCSConnector = SpearConnector
    .createConnector("PostgresToGCSConnector")
    .source(sourceType = "relational", sourceFormat = "jdbc")
    .target(targetType = "FS", targetFormat = "parquet")
    .getConnector
postgresToGCSConnector.setVeboseLogging(true)
postgresToGCSConnector
    .source("mytable", Map("driver" -> "org.postgresql.Driver", "user" -> "postgres_user", "password" -> "mysecretpassword", "url" -> "jdbc:postgresql://postgres:5432/pgdb"))
    .saveAs("__tmp__")
    .transformSql(
      """
        |select *
        |from __tmp__""".stripMargin)
    .targetFS(destinationFilePath = "gs://testbucketspear/ora_data", SaveMode.Overwrite)

postgresToGCSConnector.stop()
```

## Contributions and License
#### License
Software Licensed under the [Apache License 2.0](LICENSE)
#### Author
Anudeep Konaboina <krantianudeep@gmail.com>
#### Contributor
Kayan Deshi <kalyan.mgit@gmail.com>

To view more connectors visit website [Click here](https://anudeepkonaboina.github.io/spear-framework/)


