package com.github.edge.roman.spear.connectors.targetjdbc

import com.databricks.spark.xml.XmlDataFrameReader
import com.github.edge.roman.spear.SpearConnector
import com.github.edge.roman.spear.commons.SpearCommons
import com.github.edge.roman.spear.connectors.{AbstractConnector, TargetJDBCConnector}
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.types.StructType

import java.util.Properties

class FiletoJDBC(sourceFormat: String, destFormat: String)  extends AbstractConnector with TargetJDBCConnector {

  override def source(sourcePath: String, params: Map[String, String], schema: StructType): FiletoJDBC = {
    val paramsWithSchema = params + ("customSchema" -> schema.toString())
    source(sourcePath, paramsWithSchema)
  }

  override def source(sourceFilePath: String, params: Map[String, String]): FiletoJDBC = {
    sourceFormat match {
      case "csv" =>
        val df = SpearConnector.spark.read.options(params).csv(sourceFilePath)
        this.df = df
      case "avro" | "parquet" =>
        val df = SpearConnector.spark.read.format(sourceFormat).options(params).load(sourceFilePath)
        this.df = df
      case "json" =>
        val df = SpearConnector.spark.read.options(params).json(sourceFilePath)
        this.df = df
      case "tsv" =>
        val _params = params + ("sep" -> "\t")
        val df = SpearConnector.spark.read.options(_params).csv(sourceFilePath)
        this.df = df
      case "xml" =>
        val df = SpearConnector.spark.read.format("com.databricks.spark.xml").options(params).xml(sourceFilePath)
        this.df = df
      case _ =>
        throw new Exception("Invalid source format provided.")
    }
    logger.info(s"Reading source file: ${sourceFilePath} with format: ${sourceFormat} status:${SpearCommons.SuccessStatus}")
    if (this.verboseLogging) this.df.show(this.numRows, false)
    this
  }

  override def targetJDBC(tableName: String, props: Properties, saveMode: SaveMode): Unit = {
    destFormat match {
      case "soql" =>
        this.df.write.format(SpearCommons.SalesForceFormat)
          .option("username", props.get("username").toString)
          .option("password", props.get("password").toString)
          .option("sfObject", tableName).save()
      case "saql" =>
        this.df.write.format(SpearCommons.SalesForceFormat)
          .option("username", props.get("username").toString)
          .option("password", props.get("password").toString)
          .option("datasetName", tableName).save()
      case _ =>
        this.df.write.mode(saveMode).jdbc(props.get("url").toString, tableName, props)
    }
    logger.info(s"Write data to table/object ${tableName} completed with status:${SpearCommons.SuccessStatus} ")
    if (this.verboseLogging) this.df.show(this.numRows, false)
  }

  override def targetSql(sqlText: String, props: Properties, saveMode: SaveMode): Unit = {
    this.df.sqlContext.sql(sqlText)
  }
}
