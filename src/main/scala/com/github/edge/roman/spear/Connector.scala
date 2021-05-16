package com.github.edge.roman.spear

import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import java.util.Properties

trait Connector {
  var sparkSession: SparkSession = null
  var df: DataFrame = null

  def init(appName: String): Connector = {
    sparkSession = SparkSession.builder().appName(appName).enableHiveSupport().getOrCreate()
    this
  }

  def saveAs(alias: String): Connector = {
    this.df.createOrReplaceTempView(alias)
    df.show(10, false)
    this
  }

  def toDF: DataFrame=this.df

  def cacheData(): Connector= {
    this.df.cache()
    this
  }

  def stop(): Unit = this.sparkSession.stop()

  def source(sourceObject: String, params: Map[String, String] = Map()): Connector

  def sourceSql(params: Map[String, String], sqlText: String): Connector

  def transformSql(sqlText: String): Connector

  def targetFS(destinationFilePath: String, saveAsTable: String, saveMode: SaveMode): Unit

  def targetFS(destinationFilePath: String, saveMode: SaveMode): Unit

  def targetFS(destinationPath: String, params: Map[String, String]): Connector

  def targetFS(destinationFilePath: String): Connector

  def targetJDBC(tableName: String, props: Properties, saveMode: SaveMode): Unit

}
