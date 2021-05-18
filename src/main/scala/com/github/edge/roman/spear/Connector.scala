package com.github.edge.roman.spear

import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import java.util.Properties

trait Connector {

  var df: DataFrame = null

  def saveAs(alias: String): Connector = {
    this.df.createOrReplaceTempView(alias)
    this
  }

  def cacheData(): Connector = {
    this.df.cache()
    this
  }

  def repartition(n: Int): Connector = {
    this.df.repartition(n)
    this
  }

  def coalesce(n: Int): Connector = {
    this.df.coalesce(n)
    this
  }

  def toDF: DataFrame = this.df

  def stop(): Unit = SpearConnector.spark.stop()

  def source(sourceObject: String, params: Map[String, String] = Map()): Connector

  def source(sourceObject: String, params: Map[String, String], schema: StructType): Connector

  def sourceSql(params: Map[String, String], sqlText: String): Connector

  def transformSql(sqlText: String): Connector

  def targetFS(destinationFilePath: String, saveAsTable: String, saveMode: SaveMode): Unit

  def targetFS(destinationFilePath: String, saveMode: SaveMode): Unit

  def targetFS(destinationPath: String, params: Map[String, String]): Unit

  def targetFS(destinationFilePath: String): Unit

  def targetJDBC(tableName: String, props: Properties, saveMode: SaveMode): Unit

  def targetSql(sqlText: String, props: Properties, saveMode: SaveMode): Unit

}

