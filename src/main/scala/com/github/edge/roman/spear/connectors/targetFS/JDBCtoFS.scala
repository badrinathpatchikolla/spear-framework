package com.github.edge.roman.spear.connectors.targetFS

import com.github.edge.roman.spear.{Connector, SpearConnector}
import com.github.edge.roman.spear.connectors.TargetFSConnector
import org.apache.log4j.Logger
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.types.StructType

import java.util.Properties

class JDBCtoFS(sourceFormat: String, destFormat: String) extends TargetFSConnector {
  val logger: Logger = Logger.getLogger(this.getClass.getName)

  override def source(sourceObject: String, params: Map[String, String], schema: StructType): Connector = {
    val paramsWithSchema = params + ("customSchema" -> schema.toString())
    source(sourceObject, paramsWithSchema)
  }

  override def source(tableName: String, params: Map[String, String]): JDBCtoFS = {
    sourceFormat match {
      case "soql" =>
        throw new Exception(tableName +"object cannot be loaded directly...instead use sourceSql function with soql query")
      case _ =>
        val df = SpearConnector.spark.read.format(sourceFormat).option("dbtable", tableName).options(params).load()
        this.df = df
    }
    this
  }

  override def sourceSql(params: Map[String, String], sqlText: String): JDBCtoFS = {
    sourceFormat match {
      case "soql" =>
        SpearConnector.spark.read.format("com.springml.spark.salesforce").option("soql", s"$sqlText").options(params).load()
      case _ =>
        val _df = SpearConnector.spark.read.format(sourceFormat).option("dbtable", s"($sqlText)temp").options(params).load()
        this.df = _df
    }
    this
  }

  override def transformSql(sqlText: String): JDBCtoFS = {
    val _df = this.df.sqlContext.sql(sqlText)
    this.df = _df
    this
  }

  override def targetFS(destinationFilePath: String, tableName: String, saveMode: SaveMode): Unit = {
    if (destinationFilePath.isEmpty) {
      this.df.write.format(destFormat).mode(saveMode).saveAsTable(tableName)
    } else {
      this.df.write.format(destFormat).mode(saveMode).option("path", destinationFilePath).saveAsTable(tableName)
    }
    val targetDF = SpearConnector.spark.sql("select * from " + tableName)
    targetDF.show(10, false)
  }

  override def targetFS(destinationFilePath: String, saveMode: SaveMode): Unit = {
    if (destinationFilePath.isEmpty) {
      throw new Exception("Empty file path specified:" + destinationFilePath)
    } else {
      this.df.write.format(destFormat).mode(saveMode).option("path", destinationFilePath).save()
    }
  }

  override def targetSql(sqlText: String, props: Properties, saveMode: SaveMode): Unit = {
    this.df.sqlContext.sql(sqlText)
  }
}

