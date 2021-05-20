package com.github.edge.roman.spear.connectors.targetFS

import com.github.edge.roman.spear.commons.SpearCommons
import com.github.edge.roman.spear.{Connector, SpearConnector}
import com.github.edge.roman.spear.connectors.{AbstractConnector, TargetFSConnector}
import org.apache.spark.sql.{DataFrame, SaveMode}
import org.apache.spark.sql.types.StructType

import java.util.Properties

class JDBCtoFS(sourceFormat: String, destFormat: String) extends AbstractConnector with TargetFSConnector {

  override def source(sourceObject: String, params: Map[String, String], schema: StructType): Connector = {
    val paramsWithSchema = params + ("customSchema" -> schema.toString())
    source(sourceObject, paramsWithSchema)
  }

  override def source(tableName: String, params: Map[String, String]): JDBCtoFS = {
    sourceFormat match {
      case "soql" | "saql" =>
        throw new NoSuchMethodException(s"Salesforce object ${tableName} cannot be loaded directly.Instead use sourceSql function with soql/saql query to load the data")
      case _ =>
        val df = SpearConnector.spark.read.format(sourceFormat).option("dbtable", tableName).options(params).load()
        this.df = df
    }
    logger.info(s"Reading source table: ${tableName} with format: ${sourceFormat} status:${SpearCommons.SuccessStatus}")
    if (this.verboseLogging)this.df.show(this.numRows, false)
    this
  }

  override def sourceSql(params: Map[String, String], sqlText: String): JDBCtoFS = {
    sourceFormat match {
      case "soql" | "saql" =>
        var _df: DataFrame = null
        if (sourceFormat.equals("soql")) {
          _df = SpearConnector.spark.read.format("com.springml.spark.salesforce").option("soql", s"$sqlText").options(params).load()
        } else {
          _df=SpearConnector.spark.read.format("com.springml.spark.salesforce").option("saql", s"$sqlText").options(params).load()
        }
        this.df=_df
      case _ =>
        val _df = SpearConnector.spark.read.format(sourceFormat).option("dbtable", s"($sqlText)temp").options(params).load()
        this.df = _df
    }
    logger.info(s"Executing source query: ${sqlText} with format: ${sourceFormat} status:${SpearCommons.SuccessStatus}")
    if (this.verboseLogging)this.df.show(this.numRows, false)
    this
  }


  override def targetFS(destinationFilePath: String, tableName: String, saveMode: SaveMode): Unit = {
    if (destinationFilePath.isEmpty) {
      this.df.write.format(destFormat).mode(saveMode).saveAsTable(tableName)
    } else {
      this.df.write.format(destFormat).mode(saveMode).option("path", destinationFilePath).saveAsTable(tableName)
    }
    logger.info(s"Write data to target path: ${destinationFilePath} with format: ${sourceFormat} and saved as table ${tableName} completed with status:${SpearCommons.SuccessStatus}")
    if (this.verboseLogging) {
      val targetDF = SpearConnector.spark.sql("select * from " + tableName)
      targetDF.show(this.numRows, false)
    }
  }

  override def targetFS(destinationFilePath: String, saveMode: SaveMode): Unit = {
    if (destinationFilePath.isEmpty) {
      throw new Exception("Empty file path specified:" + destinationFilePath)
    } else {
      this.df.write.format(destFormat).mode(saveMode).option("path", destinationFilePath).save()
      logger.info(s"Write data to target path: ${destinationFilePath} with format: ${sourceFormat} completed with status:${SpearCommons.SuccessStatus}")
    }
  }

  override def targetSql(sqlText: String, props: Properties, saveMode: SaveMode): Unit = {
    this.df.sqlContext.sql(sqlText)
  }
}

