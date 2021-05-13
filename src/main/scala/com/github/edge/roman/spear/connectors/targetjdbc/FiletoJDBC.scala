package com.github.edge.roman.spear.connectors.targetjdbc

import com.databricks.spark.xml.XmlDataFrameReader
import com.github.edge.roman.spear.{Connector, SpearConnector}
import com.github.edge.roman.spear.connectors.TargetJDBCConnector
import com.typesafe.scalalogging.LazyLogging
import org.apache.log4j.Logger
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.types.StructType

import java.util.Properties

class FiletoJDBC(sourceFormat: String, destFormat: String) extends TargetJDBCConnector with LazyLogging {


  override def source(sourcePath: String, params: Map[String, String], schema: StructType): FiletoJDBC = {
    val paramsWithSchema = params+("customSchema" -> schema.toString())
    source(sourcePath,paramsWithSchema)
  }

  override def source(sourcePath: String, params: Map[String, String]): FiletoJDBC = {
    sourceFormat match {
      case "csv" =>
        val df = SpearConnector.spark.read.options(params).csv(sourcePath)
        this.df = df
      case "avro" =>
        val df = SpearConnector.spark.read.format("avro").options(params).load(sourcePath)
        this.df = df
      case "parquet" =>
        val df = SpearConnector.spark.read.format("parquet").options(params).load(sourcePath)
        this.df = df
      case "json" =>
        val df = SpearConnector.spark.read.options(params).json(sourcePath)
        this.df = df
      case "tsv" =>
        val df = SpearConnector.spark.read.options(params).csv(sourcePath)
        this.df = df
      case "xml" =>
        val df = SpearConnector.spark.read.format("com.databricks.spark.xml").options(params).xml(sourcePath)
        this.df = df
      case _ =>
        throw new Exception("Invalid source format provided.")
    }
    this
  }

  override def transformSql(sqlText: String): FiletoJDBC = {
    this.df = this.df.sqlContext.sql(sqlText)
    this
  }

  override def targetJDBC(tableName: String, props: Properties, saveMode: SaveMode): Unit = {
    this.df.write.mode(saveMode).jdbc(props.get("url").toString, tableName, props)
    showTargetData(tableName: String, props: Properties)
  }

  def showTargetData(tableName: String, props: Properties): Unit = {
    SpearConnector.spark.read.jdbc(props.get("url").toString, tableName, props).show(10, false)
  }
}
