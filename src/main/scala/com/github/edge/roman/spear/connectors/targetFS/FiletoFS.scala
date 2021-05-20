package com.github.edge.roman.spear.connectors.targetFS

import com.databricks.spark.xml.XmlDataFrameReader
import com.github.edge.roman.spear.SpearConnector
import com.github.edge.roman.spear.commons.SpearCommons
import com.github.edge.roman.spear.connectors.{AbstractConnector, TargetFSConnector}
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.types.StructType

class FiletoFS(sourceFormat: String, destFormat: String) extends AbstractConnector with TargetFSConnector {

  override def source(sourceFilePath: String, params: Map[String, String], schema: StructType): FiletoFS = {
    val paramsWithSchema = params + (SpearCommons.CustomSchema -> schema.toString())
    source(sourceFilePath, paramsWithSchema)
  }

  override def source(sourceFilePath: String, params: Map[String, String]): FiletoFS = {
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
    if (this.verboseLogging) this.df.show(10, false)
    this
  }


  override def targetFS(destinationFilePath: String, tableName: String, saveMode: SaveMode): Unit = {
    if (destinationFilePath.isEmpty) {
      this.df.write.format(destFormat).mode(saveMode).saveAsTable(tableName)
    } else {
      this.df.write.format(destFormat).mode(saveMode).option(SpearCommons.Path, destinationFilePath).saveAsTable(tableName)
    }
    logger.info(s"Write data to target path: ${destinationFilePath} with format: ${sourceFormat} and saved as table ${tableName} completed with status:${SpearCommons.SuccessStatus}")
    if (this.verboseLogging) {
      val targetDF = SpearConnector.spark.sql("select * from " + tableName)
      targetDF.show(10, false)
    }
  }

  override def targetFS(destinationFilePath: String, saveMode: SaveMode): Unit = {
    if (destinationFilePath.isEmpty) {
      throw new Exception("Empty file path specified:" + destinationFilePath)
    } else {
      this.df.write.format(destFormat).mode(saveMode).option(SpearCommons.Path, destinationFilePath).save()
      logger.info(s"Write data to target path: ${destinationFilePath} with format: ${sourceFormat} completed with status:${SpearCommons.SuccessStatus}")
    }
  }
}